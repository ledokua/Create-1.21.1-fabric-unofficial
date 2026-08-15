package com.simibubi.create.foundation.platform;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/** The running server (integrated or dedicated), tracked via Fabric's server lifecycle events. */
public final class ServerLifecycleHooks {

	@Nullable
	private static MinecraftServer currentServer;

	private ServerLifecycleHooks() {}

	/** Call once from mod init. */
	public static void init() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			if (currentServer == server)
				currentServer = null;
		});
	}

	@Nullable
	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}
}
