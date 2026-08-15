package com.simibubi.create.foundation.event.tick;

import com.simibubi.create.foundation.event.entity.player.PlayerEvent;

import net.minecraft.world.entity.player.Player;

/** Per-player tick hooks; fired from Player#tick (both sides). */
public abstract class PlayerTickEvent extends PlayerEvent {

	protected PlayerTickEvent(Player player) {
		super(player);
	}

	public static class Pre extends PlayerTickEvent {
		public Pre(Player player) {
			super(player);
		}
	}

	public static class Post extends PlayerTickEvent {
		public Post(Player player) {
			super(player);
		}
	}
}
