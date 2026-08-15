package com.simibubi.create.foundation.event.client;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

/**
 * Lifecycle event: register HUD layers. NeoForge orders layers relative to vanilla ones
 * ({@link VanillaGuiLayers}); Fabric's HudRenderCallback draws after all vanilla layers, which is what
 * Create's overlays want anyway (they're all "above" something). Layers collected here are drawn by
 * CreateEventHooks' HudRenderCallback in registration order.
 */
public class RegisterGuiLayersEvent extends Event {

	public void registerAbove(ResourceLocation other, ResourceLocation id, LayeredDraw.Layer layer) {
		CreateGuiLayers.add(id, layer);
	}

	public void registerBelow(ResourceLocation other, ResourceLocation id, LayeredDraw.Layer layer) {
		CreateGuiLayers.add(id, layer);
	}

	public void registerAboveAll(ResourceLocation id, LayeredDraw.Layer layer) {
		CreateGuiLayers.add(id, layer);
	}

	public void registerBelowAll(ResourceLocation id, LayeredDraw.Layer layer) {
		CreateGuiLayers.add(id, layer);
	}
}
