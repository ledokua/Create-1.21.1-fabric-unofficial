package com.simibubi.create.foundation.platform;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/** UUID → last seen username, filled from player joins (NeoForge persists this to disk; in-memory is enough for Create's use). */
public final class UsernameCache {

	private static final Map<UUID, String> CACHE = new ConcurrentHashMap<>();

	private UsernameCache() {}

	public static void init() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CACHE.put(handler.getPlayer().getUUID(), handler.getPlayer().getGameProfile().getName()));
	}

	@Nullable
	public static String getLastKnownUsername(UUID uuid) {
		return CACHE.get(uuid);
	}

	public static boolean containsUUID(UUID uuid) {
		return CACHE.containsKey(uuid);
	}
}
