package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.simibubi.create.foundation.render.FullBrightVertexConsumer;

import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;

/**
 * Fabric port: block models drawn in GUIs by Catnip's {@code GuiGameElement} (recipe viewer animations, Create's
 * screens) get full-bright light whatever renderer buffers them; see {@link FullBrightVertexConsumer}. Without it,
 * Sodium drew presses, basins, ... black in EMI/JEI.
 */
@Mixin(targets = "net.createmod.catnip.gui.element.GuiGameElement$GuiBlockModelRenderBuilder")
public class GuiBlockModelFullBrightMixin {

	@ModifyArg(
		method = "renderModel",
		at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/client/render/model/BakedModelBufferer;bufferModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/createmod/catnip/client/render/model/ShadeSeparatedBufferSource;)V"),
		index = 5
	)
	private ShadeSeparatedBufferSource create$fullBright(ShadeSeparatedBufferSource source) {
		return (renderType, shade) -> new FullBrightVertexConsumer(source.getBuffer(renderType, shade));
	}
}
