package com.simibubi.create.foundation.event.lifecycle;

import com.simibubi.create.foundation.event.bus.Event;

import net.neoforged.fml.config.ModConfig;

/**
 * Config (re)load notifications. {@code ModConfig} here is Forge Config API Port's (a real Fabric config
 * library that keeps NeoForge's package names — see PORTING_PLAN.md); posted from its ModConfigEvents.
 */
public abstract class ModConfigEvent extends Event {

	private final ModConfig config;

	protected ModConfigEvent(ModConfig config) {
		this.config = config;
	}

	public ModConfig getConfig() {
		return config;
	}

	public static class Loading extends ModConfigEvent {
		public Loading(ModConfig config) {
			super(config);
		}
	}

	public static class Reloading extends ModConfigEvent {
		public Reloading(ModConfig config) {
			super(config);
		}
	}

	public static class Unloading extends ModConfigEvent {
		public Unloading(ModConfig config) {
			super(config);
		}
	}
}
