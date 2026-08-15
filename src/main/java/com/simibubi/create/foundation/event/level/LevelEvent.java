package com.simibubi.create.foundation.event.level;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.world.level.LevelAccessor;

/** Level lifecycle events. Fired from Fabric's ServerWorldEvents / ClientWorld hooks (see CreateEventHooks). */
public abstract class LevelEvent extends Event {

	private final LevelAccessor level;

	protected LevelEvent(LevelAccessor level) {
		this.level = level;
	}

	public LevelAccessor getLevel() {
		return level;
	}

	public static class Load extends LevelEvent {
		public Load(LevelAccessor level) {
			super(level);
		}
	}

	public static class Unload extends LevelEvent {
		public Unload(LevelAccessor level) {
			super(level);
		}
	}

	public static class Save extends LevelEvent {
		public Save(LevelAccessor level) {
			super(level);
		}
	}
}
