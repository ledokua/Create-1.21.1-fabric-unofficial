package com.simibubi.create.foundation.extension;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 */
public final class ExtensionOverrides {

	private record Key(Class<?> owner, String name, String paramsDesc) {}

	private static final Map<Key, Boolean> CACHE = new ConcurrentHashMap<>();

	private ExtensionOverrides() {}

	/** True if {@code target}'s class overrides {@code name(params)} somewhere below the interface {@code declaring}. */
	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?>... params) {
		Class<?> owner = target.getClass();
		StringBuilder desc = new StringBuilder("(");
		for (Class<?> p : params)
			desc.append(Type.getDescriptor(p));
		desc.append(')');
		String paramsDesc = desc.toString();
		return CACHE.computeIfAbsent(new Key(owner, name, paramsDesc), k -> declares(owner, declaring, name, paramsDesc, new HashSet<>()));
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
