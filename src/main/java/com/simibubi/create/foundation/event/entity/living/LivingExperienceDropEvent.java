package com.simibubi.create.foundation.event.entity.living;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class LivingExperienceDropEvent extends LivingEvent implements ICancellableEvent {

	@Nullable
	private final Player attackingPlayer;
	private final int originalExperiencePoints;
	private int droppedExperiencePoints;

	public LivingExperienceDropEvent(LivingEntity entity, @Nullable Player attackingPlayer, int originalExperience) {
		super(entity);
		this.attackingPlayer = attackingPlayer;
		this.originalExperiencePoints = originalExperience;
		this.droppedExperiencePoints = originalExperience;
	}

	public int getDroppedExperience() {
		return droppedExperiencePoints;
	}

	public void setDroppedExperience(int droppedExperience) {
		this.droppedExperiencePoints = droppedExperience;
	}

	@Nullable
	public Player getAttackingPlayer() {
		return attackingPlayer;
	}

	public int getOriginalExperience() {
		return originalExperiencePoints;
	}
}
