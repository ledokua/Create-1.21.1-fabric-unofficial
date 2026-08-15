package com.simibubi.create.foundation.event.client;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

/** HUD layers registered through {@link RegisterGuiLayersEvent}; drawn by CreateEventHooks (HudRenderCallback). */
public final class CreateGuiLayers {

	private static final Map<ResourceLocation, LayeredDraw.Layer> LAYERS = new LinkedHashMap<>();

	private CreateGuiLayers() {}

	public static void add(ResourceLocation id, LayeredDraw.Layer layer) {
		LAYERS.put(id, layer);
	}

	public static Map<ResourceLocation, LayeredDraw.Layer> layers() {
		return LAYERS;
	}
}
