package com.simibubi.create.foundation.event.tick;

import java.util.function.BooleanSupplier;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.server.MinecraftServer;

/** Server tick hooks; fired from Fabric's ServerTickEvents. */
public abstract class ServerTickEvent extends Event {

	private final BooleanSupplier hasTime;
	private final MinecraftServer server;

	protected ServerTickEvent(BooleanSupplier hasTime, MinecraftServer server) {
		this.hasTime = hasTime;
		this.server = server;
	}

	public boolean hasTime() {
		return hasTime.getAsBoolean();
	}

	public MinecraftServer getServer() {
		return server;
	}

	public static class Pre extends ServerTickEvent {
		public Pre(BooleanSupplier hasTime, MinecraftServer server) {
			super(hasTime, server);
		}
	}

	public static class Post extends ServerTickEvent {
		public Post(BooleanSupplier hasTime, MinecraftServer server) {
			super(hasTime, server);
		}
	}
}
