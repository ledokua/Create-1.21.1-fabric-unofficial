package com.simibubi.create.foundation.event.entity;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

/** Base for entity events (bridge, see {@link Event}). */
public abstract class EntityEvent extends Event {

	private final Entity entity;

	protected EntityEvent(Entity entity) {
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	/** Fired when an entity's dimensions are recomputed (Entity#refreshDimensions); handlers may override the size. */
	public static class Size extends EntityEvent {
		private final Pose pose;
		private final EntityDimensions oldSize;
		private EntityDimensions newSize;
		private final float oldEyeHeight;
		private float newEyeHeight;

		public Size(Entity entity, Pose pose, EntityDimensions oldSize, EntityDimensions newSize) {
			super(entity);
			this.pose = pose;
			this.oldSize = oldSize;
			this.newSize = newSize;
			this.oldEyeHeight = oldSize.eyeHeight();
			this.newEyeHeight = newSize.eyeHeight();
		}

		public Pose getPose() {
			return pose;
		}

		public EntityDimensions getOldSize() {
			return oldSize;
		}

		public EntityDimensions getNewSize() {
			return newSize;
		}

		public void setNewSize(EntityDimensions size) {
			setNewSize(size, false);
		}

		public void setNewSize(EntityDimensions size, boolean updateEyeHeight) {
			this.newSize = size;
			if (updateEyeHeight)
				this.newEyeHeight = size.eyeHeight();
		}

		public float getOldEyeHeight() {
			return oldEyeHeight;
		}

		public float getNewEyeHeight() {
			return newEyeHeight;
		}

		public void setNewEyeHeight(float eyeHeight) {
			this.newEyeHeight = eyeHeight;
		}
	}

	/** Fired when an entity moves into a different chunk section. */
	public static class EnteringSection extends EntityEvent {
		private final long packedOldPos;
		private final long packedNewPos;

		public EnteringSection(Entity entity, long packedOldPos, long packedNewPos) {
			super(entity);
			this.packedOldPos = packedOldPos;
			this.packedNewPos = packedNewPos;
		}

		public long getPackedOldPos() {
			return packedOldPos;
		}

		public long getPackedNewPos() {
			return packedNewPos;
		}

		public SectionPos getOldPos() {
			return SectionPos.of(packedOldPos);
		}

		public SectionPos getNewPos() {
			return SectionPos.of(packedNewPos);
		}

		public boolean didChunkChange() {
			return SectionPos.x(packedOldPos) != SectionPos.x(packedNewPos) || SectionPos.z(packedOldPos) != SectionPos.z(packedNewPos);
		}
	}
}
