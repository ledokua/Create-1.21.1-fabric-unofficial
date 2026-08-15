package com.simibubi.create.foundation.event.entity;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Fired when an entity is added to a level; cancelling prevents the add. */
public class EntityJoinLevelEvent extends EntityEvent implements ICancellableEvent {

	private final Level level;
	private final boolean loadedFromDisk;

	public EntityJoinLevelEvent(Entity entity, Level level) {
		this(entity, level, false);
	}

	public EntityJoinLevelEvent(Entity entity, Level level, boolean loadedFromDisk) {
		super(entity);
		this.level = level;
		this.loadedFromDisk = loadedFromDisk;
	}

	public Level getLevel() {
		return level;
	}

	public boolean loadedFromDisk() {
		return loadedFromDisk;
	}
}
