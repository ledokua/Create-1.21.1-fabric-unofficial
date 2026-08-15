package com.simibubi.create.foundation.event.entity.living;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Fired when a living entity is about to take damage; handlers may adjust the amount or cancel. */
public class LivingIncomingDamageEvent extends LivingEvent implements ICancellableEvent {

	private final DamageSource source;
	private final float originalAmount;
	private float amount;

	public LivingIncomingDamageEvent(LivingEntity entity, DamageSource source, float amount) {
		super(entity);
		this.source = source;
		this.originalAmount = amount;
		this.amount = amount;
	}

	public DamageSource getSource() {
		return source;
	}

	public float getOriginalAmount() {
		return originalAmount;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
}
