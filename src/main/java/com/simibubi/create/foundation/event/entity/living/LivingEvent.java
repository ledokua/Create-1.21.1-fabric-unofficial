package com.simibubi.create.foundation.event.entity.living;

import com.simibubi.create.foundation.event.entity.EntityEvent;

import net.minecraft.world.entity.LivingEntity;

public abstract class LivingEvent extends EntityEvent {

	private final LivingEntity livingEntity;

	protected LivingEvent(LivingEntity entity) {
		super(entity);
		this.livingEntity = entity;
	}

	@Override
	public LivingEntity getEntity() {
		return livingEntity;
	}

	/** Lets handlers scale how visible {@link #getEntity()} is to {@link #getLookingEntity()}. */
	public static class LivingVisibilityEvent extends LivingEvent {
		private final net.minecraft.world.entity.Entity lookingEntity;
		private double visibilityModifier;

		public LivingVisibilityEvent(LivingEntity livingEntity, net.minecraft.world.entity.Entity lookingEntity, double originalMultiplier) {
			super(livingEntity);
			this.lookingEntity = lookingEntity;
			this.visibilityModifier = originalMultiplier;
		}

		public void modifyVisibility(double mod) {
			visibilityModifier *= mod;
		}

		public double getVisibilityModifier() {
			return visibilityModifier;
		}

		public net.minecraft.world.entity.Entity getLookingEntity() {
			return lookingEntity;
		}
	}
}
