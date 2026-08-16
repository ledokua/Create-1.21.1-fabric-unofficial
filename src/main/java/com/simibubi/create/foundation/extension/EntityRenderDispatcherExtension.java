package com.simibubi.create.foundation.extension;

import java.util.Map;

import com.simibubi.create.foundation.mixin.accessor.EntityRenderDispatcherAccessor;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;

/** Injected onto {@code EntityRenderDispatcher}: NeoForge's {@code getSkinMap()} (vanilla keeps the map private). */
public interface EntityRenderDispatcherExtension {
	default Map<PlayerSkin.Model, EntityRenderer<? extends Player>> getSkinMap() {
		return ((EntityRenderDispatcherAccessor) this).create$getPlayerRenderers();
	}
}
