package com.simibubi.create.foundation.event.entity.player;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Fired when a player attacks an entity; cancelling suppresses the attack. */
public class AttackEntityEvent extends PlayerEvent implements ICancellableEvent {

	private final Entity target;

	public AttackEntityEvent(Player player, Entity target) {
		super(player);
		this.target = target;
	}

	public Entity getTarget() {
		return target;
	}
}
