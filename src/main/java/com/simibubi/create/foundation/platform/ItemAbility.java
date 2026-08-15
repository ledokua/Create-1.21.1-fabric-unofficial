package com.simibubi.create.foundation.platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A named tool action (NeoForge's ItemAbility, Create-owned). See {@link ItemAbilities}. */
public final class ItemAbility {

	private static final Map<String, ItemAbility> ABILITIES = new ConcurrentHashMap<>();

	public static ItemAbility get(String name) {
		return ABILITIES.computeIfAbsent(name, ItemAbility::new);
	}

	private final String name;

	private ItemAbility(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "ItemAbility[" + name + "]";
	}
}
