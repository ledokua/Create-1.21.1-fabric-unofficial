package com.simibubi.create.foundation.transfer.fluid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Named sound slot on a {@link FluidType} (bucket fill/empty, ...). Create-owned (was NeoForge's). */
public final class SoundAction {

	private static final Map<String, SoundAction> ACTIONS = new ConcurrentHashMap<>();

	public static SoundAction get(String name) {
		return ACTIONS.computeIfAbsent(name, SoundAction::new);
	}

	private final String name;

	private SoundAction(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "SoundAction[" + name + "]";
	}
}
