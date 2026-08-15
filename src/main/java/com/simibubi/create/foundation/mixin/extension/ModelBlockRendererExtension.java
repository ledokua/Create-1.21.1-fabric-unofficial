package com.simibubi.create.foundation.mixin.extension;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.model.ModelDataContext;
import com.simibubi.create.foundation.model.data.ModelData;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/** Injected onto {@code ModelBlockRenderer}: NeoForge's data/layer-aware {@code tesselateBlock} overload (see {@link ModelDataContext}). */
public interface ModelBlockRendererExtension {

	default void tesselateBlock(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer consumer,
		boolean checkSides, RandomSource random, long seed, int packedOverlay, ModelData modelData, RenderType renderType) {
		ModelDataContext.with(modelData, renderType, () ->
			((ModelBlockRenderer) this).tesselateBlock(level, model, state, pos, poseStack, consumer, checkSides, random, seed, packedOverlay));
	}
}
