package com.simibubi.create.foundation.event.entity;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** Fired when an entity starts/stops riding another; cancellable. */
public class EntityMountEvent extends EntityEvent implements ICancellableEvent {

	private final Entity entityMounting;
	private final Entity entityBeingMounted;
	private final Level level;
	private final boolean isMounting;

	public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, Level level, boolean isMounting) {
		super(entityMounting);
		this.entityMounting = entityMounting;
		this.entityBeingMounted = entityBeingMounted;
		this.level = level;
		this.isMounting = isMounting;
	}

	public boolean isMounting() {
		return isMounting;
	}

	public boolean isDismounting() {
		return !isMounting;
	}

	public Entity getEntityMounting() {
		return entityMounting;
	}

	public Entity getEntityBeingMounted() {
		return entityBeingMounted;
	}

	public Level getLevel() {
		return level;
	}
}
