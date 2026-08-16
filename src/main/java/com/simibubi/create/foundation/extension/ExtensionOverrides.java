package com.simibubi.create.foundation.extension;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cheap "does this class override the injected extension method?" check, so the hook mixins only route through the
 * NeoForge-shaped methods for blocks/items that actually implement them (everything else keeps vanilla's exact path,
 * including other mods' mixins on it).
 */
public final class ExtensionOverrides {

	private record Key(Class<?> owner, String name, int arity) {}

	private static final Map<Key, Boolean> CACHE = new ConcurrentHashMap<>();

	private ExtensionOverrides() {}

	/** True if {@code target}'s class overrides {@code name(params)} somewhere below the interface {@code declaring}. */
	public static boolean overrides(Object target, Class<?> declaring, String name, Class<?>... params) {
		Class<?> owner = target.getClass();
		return CACHE.computeIfAbsent(new Key(owner, name, params.length), k -> {
			try {
				Method m = owner.getMethod(name, params);
				return m.getDeclaringClass() != declaring;
			} catch (NoSuchMethodException e) {
				return false;
			}
		});
	}
}
