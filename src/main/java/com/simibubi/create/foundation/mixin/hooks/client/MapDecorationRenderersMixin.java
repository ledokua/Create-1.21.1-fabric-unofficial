package com.simibubi.create.foundation.mixin.hooks.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.event.client.map.IMapDecorationRenderer;
import com.simibubi.create.foundation.event.client.map.RegisterMapDecorationRenderersEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * NeoForge's custom map decoration renderers ({@code MapDecorationRendererManager}): decorations with a registered
 * renderer (Create's train stations) are drawn by it and withheld from vanilla's loop.
 */
@Mixin(targets = "net.minecraft.client.gui.MapRenderer$MapInstance")
public class MapDecorationRenderersMixin {
	@WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getDecorations()Ljava/lang/Iterable;"))
	private Iterable<MapDecoration> create$renderCustomDecorations(MapItemSavedData data, Operation<Iterable<MapDecoration>> original,
		PoseStack poseStack, MultiBufferSource bufferSource, boolean inItemFrame, int packedLight) {
		List<MapDecoration> vanilla = new ArrayList<>();
		int index = 0;
		for (MapDecoration decoration : original.call(data)) {
			IMapDecorationRenderer renderer = RegisterMapDecorationRenderersEvent.get(decoration.type().value());
			if (renderer == null || !renderer.render(decoration, poseStack, bufferSource, data, Minecraft.getInstance().gameRenderer.getMapRenderer().decorationTextures, inItemFrame, packedLight, index))
				vanilla.add(decoration);
			index++;
		}
		return vanilla;
	}
}
