package com.simibubi.create.foundation.mixin.extension;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.ModelDataContext;
import com.simibubi.create.foundation.model.data.ModelData;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.world.level.block.state.BlockState;

/** Injected onto {@code BlockRenderDispatcher}: NeoForge's data/layer-aware {@code renderSingleBlock}. */
public interface BlockRenderDispatcherExtension {

	default void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, ModelData modelData, RenderType renderType) {
		ModelDataContext.with(modelData, renderType, () ->
			((BlockRenderDispatcher) this).renderSingleBlock(state, poseStack, bufferSource, packedLight, packedOverlay));
	}
}
