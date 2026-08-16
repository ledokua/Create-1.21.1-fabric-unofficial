package com.simibubi.create.foundation.event.bus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;

/**
 * Minimal event bus: listeners keyed by event class; {@link #post} walks the event's class hierarchy so a
 * listener on {@code PlayerInteractEvent} also receives {@code PlayerInteractEvent.RightClickBlock} (same as
 * upstream). Ordering: priority, then registration order. Not optimised for very high frequency events
 * beyond a per-class dispatch cache; that's plenty for Create's ~120 handlers.
 */
public class EventBus implements IEventBus {

	private record Listener(EventPriority priority, boolean receiveCanceled, Consumer<Event> consumer, Object owner, long seq) {}

	private final Map<Class<?>, List<Listener>> listeners = new ConcurrentHashMap<>();
	private final Map<Class<?>, List<Listener>> dispatchCache = new ConcurrentHashMap<>();
	private long seq;

	@Override
	public <T extends Event> void addListener(Consumer<T> consumer) {
		addListener(EventPriority.NORMAL, false, consumer);
	}

	@Override
	public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
		addListener(priority, false, consumer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer) {
		Class<T> type = (Class<T>) resolveLambdaEventType(consumer);
		addListener(priority, receiveCanceled, type, consumer);
	}

	@Override
	public <T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer) {
		addListener(EventPriority.NORMAL, false, eventType, consumer);
	}

	@Override
	@SuppressWarnings("unchecked")
	public synchronized <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer) {
		listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
			.add(new Listener(priority, receiveCanceled, (Consumer<Event>) consumer, consumer, seq++));
		dispatchCache.clear();
	}

	@Override
	public synchronized void register(Object target) {
		boolean isStatic = target instanceof Class<?>;
		Class<?> clazz = isStatic ? (Class<?>) target : target.getClass();
		int found = 0;
		for (HandlerMethod hm : scanHandlers(clazz)) {
			if (hm.isStatic() != isStatic)
				continue;
			Class<?> paramType;
			try {
				paramType = Class.forName(hm.paramType(), false, clazz.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("@SubscribeEvent method " + clazz.getName() + "#" + hm.name() + " has an unloadable event parameter " + hm.paramType(), e);
			}
			if (!Event.class.isAssignableFrom(paramType))
				throw new IllegalArgumentException("@SubscribeEvent method " + clazz.getName() + "#" + hm.name() + " must take exactly one Event parameter");
			@SuppressWarnings("unchecked")
			Class<? extends Event> eventType = (Class<? extends Event>) paramType;
			MethodHandle handle;
			try {
				MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
				java.lang.invoke.MethodType type = java.lang.invoke.MethodType.methodType(void.class, paramType);
				handle = isStatic ? lookup.findStatic(clazz, hm.name(), type) : lookup.findVirtual(clazz, hm.name(), type).bindTo(target);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Cannot access @SubscribeEvent method " + clazz.getName() + "#" + hm.name(), e);
			}
			MethodHandle finalHandle = handle;
			String description = clazz.getName() + "#" + hm.name();
			Consumer<Event> consumer = event -> {
				try {
					finalHandle.invoke(event);
				} catch (RuntimeException | Error e) {
					throw e;
				} catch (Throwable t) {
					throw new RuntimeException("Exception in event handler " + description, t);
				}
			};
			listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
				.add(new Listener(hm.priority(), hm.receiveCanceled(), consumer, target, seq++));
			found++;
		}
		if (found == 0)
			Create.LOGGER.warn("Registered {} on the event bus but it has no @SubscribeEvent {} methods", clazz.getName(), isStatic ? "static" : "instance");
		dispatchCache.clear();
	}

	private record HandlerMethod(String name, String paramType, boolean isStatic, EventPriority priority, boolean receiveCanceled) {}

	/**
	 * Finds {@code @SubscribeEvent} methods by reading the class file instead of {@code getDeclaredMethods()}: reflection
	 * resolves every method's parameter types, and on a dedicated server that trips over the leftover lambda bodies of
	 * Fabric-stripped {@code @Environment(CLIENT)} handlers (upstream's common classes mix in client-only handlers).
	 */
	private static List<HandlerMethod> scanHandlers(Class<?> clazz) {
		List<HandlerMethod> result = new ArrayList<>();
		String resource = clazz.getName().replace('.', '/') + ".class";
		try (java.io.InputStream in = clazz.getClassLoader().getResourceAsStream(resource)) {
			if (in == null)
				throw new IllegalStateException("Cannot read class file for " + clazz.getName());
			org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(in.readAllBytes());
			String annotationDesc = org.objectweb.asm.Type.getDescriptor(SubscribeEvent.class);
			String envDesc = org.objectweb.asm.Type.getDescriptor(net.fabricmc.api.Environment.class);
			String currentEnv = net.fabricmc.loader.api.FabricLoader.getInstance().getEnvironmentType().name();
			reader.accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
				@Override
				public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
					org.objectweb.asm.Type[] args = org.objectweb.asm.Type.getArgumentTypes(descriptor);
					boolean isStatic = (access & org.objectweb.asm.Opcodes.ACC_STATIC) != 0;
					return new org.objectweb.asm.MethodVisitor(org.objectweb.asm.Opcodes.ASM9) {
						boolean subscribe;
						boolean strippedForEnv;
						EventPriority priority = EventPriority.NORMAL;
						boolean receiveCanceled;

						@Override
						public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
							if (desc.equals(envDesc)) {
								// Fabric strips @Environment methods of the other side at class load; mirror that here
								return new org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
									@Override
									public void visitEnum(String n, String d, String value) {
										if (!value.equals(currentEnv))
											strippedForEnv = true;
									}
								};
							}
							if (!desc.equals(annotationDesc))
								return null;
							subscribe = true;
							return new org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
								@Override
								public void visitEnum(String n, String d, String value) {
									if (n.equals("priority"))
										priority = EventPriority.valueOf(value);
								}

								@Override
								public void visit(String n, Object value) {
									if (n.equals("receiveCanceled"))
										receiveCanceled = (Boolean) value;
								}
							};
						}

						@Override
						public void visitEnd() {
							if (!subscribe || strippedForEnv)
								return;
							if (args.length != 1)
								throw new IllegalArgumentException("@SubscribeEvent method " + clazz.getName() + "#" + name + " must take exactly one Event parameter");
							result.add(new HandlerMethod(name, args[0].getClassName(), isStatic, priority, receiveCanceled));
						}
					};
				}
			}, org.objectweb.asm.ClassReader.SKIP_CODE | org.objectweb.asm.ClassReader.SKIP_DEBUG | org.objectweb.asm.ClassReader.SKIP_FRAMES);
		} catch (java.io.IOException e) {
			throw new IllegalStateException("Cannot read class file for " + clazz.getName(), e);
		}
		return result;
	}

	@Override
	public synchronized void unregister(Object target) {
		listeners.values().forEach(list -> list.removeIf(l -> l.owner == target));
		dispatchCache.clear();
	}

	@Override
	public <T extends Event> T post(T event) {
		List<Listener> toRun = dispatchCache.computeIfAbsent(event.getClass(), this::computeDispatchList);
		if (toRun.isEmpty())
			return event;
		boolean cancellable = event instanceof ICancellableEvent;
		for (Listener listener : toRun) {
			if (cancellable && !listener.receiveCanceled && ((ICancellableEvent) event).isCanceled())
				continue;
			listener.consumer.accept(event);
		}
		return event;
	}

	private List<Listener> computeDispatchList(Class<?> eventClass) {
		List<Listener> result = new ArrayList<>();
		for (Class<?> c = eventClass; c != null && Event.class.isAssignableFrom(c); c = c.getSuperclass()) {
			List<Listener> list = listeners.get(c);
			if (list != null)
				result.addAll(list);
		}
		result.sort(Comparator.<Listener>comparingInt(l -> l.priority.ordinal()).thenComparingLong(l -> l.seq));
		return List.copyOf(result);
	}

	/** Whether anyone listens for this event class (or a superclass). Cheap; used to skip constructing events. */
	public boolean hasListeners(Class<? extends Event> eventClass) {
		return !dispatchCache.computeIfAbsent(eventClass, this::computeDispatchList).isEmpty();
	}

	// ---- lambda event-type resolution ----

	private static final Map<Class<?>, Class<?>> LAMBDA_TYPE_CACHE = new IdentityHashMap<>();

	/**
	 * Resolves {@code T} of a {@code Consumer<T>} lambda/method-ref. Java erases lambda generics, so we use
	 * the same trick upstream's bus does: inspect the synthetic class's generic interfaces, falling back to
	 * a runtime probe of the single-arg method. Prefer the explicit {@link #addListener(Class, Consumer)}
	 * overload in new code.
	 */
	@Nullable
	static Class<?> resolveLambdaEventType(Consumer<?> consumer) {
		Class<?> clazz = consumer.getClass();
		Class<?> cached;
		synchronized (LAMBDA_TYPE_CACHE) {
			cached = LAMBDA_TYPE_CACHE.get(clazz);
		}
		if (cached != null)
			return cached;
		Class<?> resolved = null;
		for (Type type : clazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType pt && pt.getRawType() == Consumer.class && pt.getActualTypeArguments()[0] instanceof Class<?> arg) {
				resolved = arg;
				break;
			}
		}
		if (resolved == null) {
			// Non-generic synthetic lambda: find the single-arg accept bridge's real parameter type via
			// the SerializedLambda-free route: the most specific accept(...) declared method.
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getName().equals("accept") && m.getParameterCount() == 1 && Event.class.isAssignableFrom(m.getParameterTypes()[0])) {
					resolved = m.getParameterTypes()[0];
					break;
				}
			}
		}
		if (resolved == null)
			throw new IllegalArgumentException("Cannot resolve event type of listener " + clazz.getName()
				+ " — use addListener(Class, Consumer) instead");
		synchronized (LAMBDA_TYPE_CACHE) {
			LAMBDA_TYPE_CACHE.put(clazz, resolved);
		}
		return resolved;
	}
}
