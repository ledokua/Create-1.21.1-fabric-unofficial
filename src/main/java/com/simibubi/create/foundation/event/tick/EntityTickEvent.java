package com.simibubi.create.foundation.event.tick;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;
import com.simibubi.create.foundation.event.entity.EntityEvent;

import net.minecraft.world.entity.Entity;

/** Per-entity tick hooks; fired from an Entity#tick mixin. Pre is cancellable (skips the vanilla tick). */
public abstract class EntityTickEvent extends EntityEvent {

	protected EntityTickEvent(Entity entity) {
		super(entity);
	}

	public static class Pre extends EntityTickEvent implements ICancellableEvent {
		public Pre(Entity entity) {
			super(entity);
		}
	}

	public static class Post extends EntityTickEvent {
		public Post(Entity entity) {
			super(entity);
		}
	}
}
