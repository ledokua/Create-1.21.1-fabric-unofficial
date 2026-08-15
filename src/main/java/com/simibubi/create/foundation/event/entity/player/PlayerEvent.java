package com.simibubi.create.foundation.event.entity.player;

import com.simibubi.create.foundation.event.entity.living.LivingEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerEvent extends LivingEvent {

	private final Player player;

	protected PlayerEvent(Player player) {
		super(player);
		this.player = player;
	}

	@Override
	public Player getEntity() {
		return player;
	}

	public static class PlayerLoggedInEvent extends PlayerEvent {
		public PlayerLoggedInEvent(Player player) {
			super(player);
		}
	}

	public static class PlayerLoggedOutEvent extends PlayerEvent {
		public PlayerLoggedOutEvent(Player player) {
			super(player);
		}
	}

	public static class PlayerRespawnEvent extends PlayerEvent {
		private final boolean endConquered;

		public PlayerRespawnEvent(Player player, boolean endConquered) {
			super(player);
			this.endConquered = endConquered;
		}

		public boolean isEndConquered() {
			return endConquered;
		}
	}

	/** A player's client started tracking {@link #getTarget()}. */
	public static class StartTracking extends PlayerEvent {
		private final Entity target;

		public StartTracking(Player player, Entity target) {
			super(player);
			this.target = target;
		}

		public Entity getTarget() {
			return target;
		}
	}

	public static class StopTracking extends PlayerEvent {
		private final Entity target;

		public StopTracking(Player player, Entity target) {
			super(player);
			this.target = target;
		}

		public Entity getTarget() {
			return target;
		}
	}
}
