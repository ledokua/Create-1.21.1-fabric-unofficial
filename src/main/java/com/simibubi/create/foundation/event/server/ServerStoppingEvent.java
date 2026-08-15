package com.simibubi.create.foundation.event.server;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.server.MinecraftServer;

/** Fired from Fabric's ServerLifecycleEvents.SERVER_STOPPING. */
public class ServerStoppingEvent extends Event {

	private final MinecraftServer server;

	public ServerStoppingEvent(MinecraftServer server) {
		this.server = server;
	}

	public MinecraftServer getServer() {
		return server;
	}
}
