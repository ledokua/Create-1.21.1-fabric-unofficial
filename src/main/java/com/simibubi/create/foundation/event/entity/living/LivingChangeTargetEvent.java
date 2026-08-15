package com.simibubi.create.foundation.event.entity.living;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.entity.LivingEntity;

/** Fired when a mob picks a new attack target; handlers may change or cancel it. */
public class LivingChangeTargetEvent extends LivingEvent implements ICancellableEvent {

	@Nullable
	private final LivingEntity originalTarget;
	@Nullable
	private LivingEntity newTarget;

	public LivingChangeTargetEvent(LivingEntity entity, @Nullable LivingEntity originalTarget) {
		super(entity);
		this.originalTarget = originalTarget;
		this.newTarget = originalTarget;
	}

	@Nullable
	public LivingEntity getNewAboutToBeSetTarget() {
		return newTarget;
	}

	public void setNewAboutToBeSetTarget(@Nullable LivingEntity target) {
		this.newTarget = target;
	}

	@Nullable
	public LivingEntity getOriginalAboutToBeSetTarget() {
		return originalTarget;
	}
}
