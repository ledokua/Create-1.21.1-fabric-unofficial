package com.simibubi.create.foundation.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Cheap "does this class override the injected extension method?" check, so the hook mixins only route through the
 * NeoForge-shaped methods for blocks/items that actually implement them (everything else keeps vanilla's exact path,
 * including other mods' mixins on it).
 * <p>
 * Implemented by scanning class bytecode rather than reflection: {@code Class#getMethod} resolves every method
 * signature of the class, which fails on the dedicated server for classes with {@code @Environment(CLIENT)} methods.
 * <p>
 * Several callers sit on very hot paths (pathfinding node types, friction, piston push reaction), so a cached answer
 * must cost no allocation: answers live in a per-class table ({@link ClassValue}) keyed by the method name's identity
 * (call sites pass string literals), and the fixed-arity overloads avoid the varargs array. The descriptor string is
 * only built on a miss.
 */
public final class ExtensionOverrides {

	private static final int MAX_FIXED_PARAMS = 6;

	private static final ClassValue<Table> TABLES = new ClassValue<>() {
		@Override
		protected Table computeValue(Class<?> type) {
			return new Table(type);
		}
	};

	private ExtensionOverrides() {}

	/** True if {@code target}'s class overrides {@code name(params)} somewhere below the interface {@code declaring}. */
	public static boolean overrides(Object target, Class<?> declaring, String name) {
		return lookup(target, declaring, name, 0, null, null, null, null, null, null);
	}

	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?> p0) {
		return lookup(target, declaring, name, 1, p0, null, null, null, null, null);
	}

	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?> p0, Class<?> p1) {
		return lookup(target, declaring, name, 2, p0, p1, null, null, null, null);
	}

	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?> p0, Class<?> p1, Class<?> p2) {
		return lookup(target, declaring, name, 3, p0, p1, p2, null, null, null);
	}

	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?> p0, Class<?> p1, Class<?> p2,
		Class<?> p3) {
		return lookup(target, declaring, name, 4, p0, p1, p2, p3, null, null);
	}

	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?> p0, Class<?> p1, Class<?> p2,
		Class<?> p3, Class<?> p4) {
		return lookup(target, declaring, name, 5, p0, p1, p2, p3, p4, null);
	}

	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?> p0, Class<?> p1, Class<?> p2,
		Class<?> p3, Class<?> p4, Class<?> p5) {
		return lookup(target, declaring, name, 6, p0, p1, p2, p3, p4, p5);
	}

	/** Any arity; more than {@value #MAX_FIXED_PARAMS} parameters takes the (allocating) array path. */
	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?>... params) {
		int n = params.length;
		if (n <= MAX_FIXED_PARAMS)
			return lookup(target, declaring, name, n, n > 0 ? params[0] : null, n > 1 ? params[1] : null,
				n > 2 ? params[2] : null, n > 3 ? params[3] : null, n > 4 ? params[4] : null, n > 5 ? params[5] : null);
		return TABLES.get(target.getClass()).miss(declaring, name, params.clone());
	}

	private static boolean lookup(Object target, Class<?> declaring, String name, int n, Class<?> p0, Class<?> p1,
		Class<?> p2, Class<?> p3, Class<?> p4, Class<?> p5) {
		Table table = TABLES.get(target.getClass());
		for (Entry e = table.byName.get(name); e != null; e = e.next)
			if (e.declaring == declaring && e.matches(n, p0, p1, p2, p3, p4, p5))
				return e.result;
		Class<?>[] params = new Class<?>[n];
		Class<?>[] all = { p0, p1, p2, p3, p4, p5 };
		System.arraycopy(all, 0, params, 0, n);
		return table.miss(declaring, name, params);
	}

	/** Answers for one target class, by method-name identity. Copy-on-write: reads never lock. */
	private static final class Table {
		private final Class<?> owner;
		private volatile IdentityHashMap<String, Entry> byName = new IdentityHashMap<>();

		Table(Class<?> owner) {
			this.owner = owner;
		}

		synchronized boolean miss(Class<?> declaring, String name, Class<?>[] params) {
			IdentityHashMap<String, Entry> current = byName;
			// another thread may have filled it, or the same name arrived as a different (equal) String instance
			for (var mapping : current.entrySet()) {
				if (!mapping.getKey().equals(name))
					continue;
				for (Entry e = mapping.getValue(); e != null; e = e.next)
					if (e.declaring == declaring && e.matches(params))
						return remember(current, name, declaring, params, e.result);
			}
			StringBuilder desc = new StringBuilder("(");
			for (Class<?> p : params)
				desc.append(Type.getDescriptor(p));
			desc.append(')');
			boolean result = declares(owner, declaring, name, desc.toString(), new HashSet<>());
			return remember(current, name, declaring, params, result);
		}

		private boolean remember(IdentityHashMap<String, Entry> current, String name, Class<?> declaring, Class<?>[] params,
			boolean result) {
			for (Entry e = current.get(name); e != null; e = e.next)
				if (e.declaring == declaring && e.matches(params))
					return e.result;
			IdentityHashMap<String, Entry> copy = new IdentityHashMap<>(current);
			copy.put(name, new Entry(declaring, params, result, current.get(name)));
			byName = copy;
			return result;
		}
	}

	private record Entry(Class<?> declaring, Class<?>[] params, boolean result, Entry next) {
		boolean matches(int n, Class<?> p0, Class<?> p1, Class<?> p2, Class<?> p3, Class<?> p4, Class<?> p5) {
			Class<?>[] ps = params;
			return ps.length == n
				&& (n < 1 || ps[0] == p0) && (n < 2 || ps[1] == p1) && (n < 3 || ps[2] == p2)
				&& (n < 4 || ps[3] == p3) && (n < 5 || ps[4] == p4) && (n < 6 || ps[5] == p5);
		}

		boolean matches(Class<?>[] other) {
			if (other.length != params.length)
				return false;
			for (int i = 0; i < other.length; i++)
				if (other[i] != params[i])
					return false;
			return true;
		}
	}

	private static boolean declares(Class<?> cls, Class<?> declaring, String name, String paramsDesc, Set<Class<?>> visited) {
		if (cls == null || cls == Object.class || cls == declaring || !visited.add(cls))
			return false;
		if (declaresDirectly(cls, name, paramsDesc))
			return true;
		if (declares(cls.getSuperclass(), declaring, name, paramsDesc, visited))
			return true;
		for (Class<?> itf : cls.getInterfaces())
			if (declares(itf, declaring, name, paramsDesc, visited))
				return true;
		return false;
	}

	private static boolean declaresDirectly(Class<?> cls, String name, String paramsDesc) {
		String resource = cls.getName().replace('.', '/') + ".class";
		ClassLoader loader = cls.getClassLoader();
		try (InputStream in = loader != null ? loader.getResourceAsStream(resource) : ClassLoader.getSystemResourceAsStream(resource)) {
			if (in == null)
				return false;
			boolean[] found = { false };
			new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
				@Override
				public MethodVisitor visitMethod(int access, String mName, String mDesc, String signature, String[] exceptions) {
					if ((access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_STATIC | Opcodes.ACC_BRIDGE)) == 0 && mName.equals(name) && mDesc.startsWith(paramsDesc))
						found[0] = true;
					return null;
				}
			}, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			return found[0];
		} catch (IOException e) {
			return false;
		}
	}
}
