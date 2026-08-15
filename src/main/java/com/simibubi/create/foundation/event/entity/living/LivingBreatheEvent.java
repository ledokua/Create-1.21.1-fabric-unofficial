package com.simibubi.create.foundation.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

/** Fired every tick to decide whether the entity can breathe and how much air it gains/loses. */
public class LivingBreatheEvent extends LivingEvent {

	private boolean canBreathe;
	private int consumeAirAmount;
	private int refillAirAmount;
	private boolean canRefillAir;

	public LivingBreatheEvent(LivingEntity entity, boolean canBreathe, int consumeAirAmount, int refillAirAmount, boolean canRefillAir) {
		super(entity);
		this.canBreathe = canBreathe;
		this.consumeAirAmount = Math.max(consumeAirAmount, 0);
		this.refillAirAmount = Math.max(refillAirAmount, 0);
		this.canRefillAir = canRefillAir;
	}

	public boolean canBreathe() {
		return canBreathe;
	}

	public void setCanBreathe(boolean canBreathe) {
		this.canBreathe = canBreathe;
	}

	public boolean canRefillAir() {
		return canRefillAir;
	}

	public void setCanRefillAir(boolean canRefillAir) {
		this.canRefillAir = canRefillAir;
	}

	public int getConsumeAirAmount() {
		return consumeAirAmount;
	}

	public void setConsumeAirAmount(int amount) {
		this.consumeAirAmount = Math.max(amount, 0);
	}

	public int getRefillAirAmount() {
		return refillAirAmount;
	}

	public void setRefillAirAmount(int amount) {
		this.refillAirAmount = Math.max(amount, 0);
	}
}
