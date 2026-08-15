package com.simibubi.create.foundation.event.client.map;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.world.level.saveddata.maps.MapDecorationType;

/** Lifecycle event: register map decoration renderers. Values are consulted by a MapRenderer mixin (Phase 3). */
public class RegisterMapDecorationRenderersEvent extends Event {

	private static final Map<MapDecorationType, IMapDecorationRenderer> RENDERERS = new HashMap<>();

	public void register(MapDecorationType type, IMapDecorationRenderer renderer) {
		RENDERERS.put(type, renderer);
	}

	@Nullable
	public static IMapDecorationRenderer get(MapDecorationType type) {
		return RENDERERS.get(type);
	}
}
