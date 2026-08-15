package com.simibubi.create.foundation.event.client.map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/** Custom renderer for a map decoration type (NeoForge's; Create-owned). Consulted by a MapRenderer mixin (Phase 3). */
@FunctionalInterface
public interface IMapDecorationRenderer {

	/** @return true if the decoration was rendered and vanilla's rendering should be skipped */
	boolean render(MapDecoration decoration, PoseStack poseStack, MultiBufferSource bufferSource, MapItemSavedData mapData,
		MapDecorationTextureManager decorationTextures, boolean inItemFrame, int packedLight, int index);
}
