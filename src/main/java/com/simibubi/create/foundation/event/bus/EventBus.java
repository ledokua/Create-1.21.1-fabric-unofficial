package com.simibubi.create.foundation.event.bus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
		for (Method method : clazz.getDeclaredMethods()) {
			SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);
			if (annotation == null)
				continue;
			if (Modifier.isStatic(method.getModifiers()) != isStatic)
				continue;
			if (method.getParameterCount() != 1 || !Event.class.isAssignableFrom(method.getParameterTypes()[0]))
				throw new IllegalArgumentException("@SubscribeEvent method " + method + " must take exactly one Event parameter");
			@SuppressWarnings("unchecked")
			Class<? extends Event> eventType = (Class<? extends Event>) method.getParameterTypes()[0];
			method.setAccessible(true);
			MethodHandle handle;
			try {
				handle = MethodHandles.lookup().unreflect(method);
				if (!isStatic)
					handle = handle.bindTo(target);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot access @SubscribeEvent method " + method, e);
			}
			MethodHandle finalHandle = handle;
			Consumer<Event> consumer = event -> {
				try {
					finalHandle.invoke(event);
				} catch (RuntimeException | Error e) {
					throw e;
				} catch (Throwable t) {
					throw new RuntimeException("Exception in event handler " + method, t);
				}
			};
			listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
				.add(new Listener(annotation.priority(), annotation.receiveCanceled(), consumer, target, seq++));
			found++;
		}
		if (found == 0)
			Create.LOGGER.warn("Registered {} on the event bus but it has no @SubscribeEvent {} methods", clazz.getName(), isStatic ? "static" : "instance");
		dispatchCache.clear();
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
