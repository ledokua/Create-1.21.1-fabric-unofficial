package com.simibubi.create.foundation.extension;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.ChunkRenderTypeSet;
import com.simibubi.create.foundation.model.data.ModelData;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Injected onto {@code BakedModel}: NeoForge's data/layer-aware overloads, so Create can call them on any
 * model. Defaults delegate to the vanilla methods; {@code IDynamicBakedModel}/{@code BakedModelWrapper}
 * override them for real. (Fabric's own FRAPI extension lives alongside; both are plain default methods.)
 */
public interface BakedModelExtension {

	private BakedModel self() {
		return (BakedModel) this;
	}

	default @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		return self().getQuads(state, side, rand);
	}

	default @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
		return modelData;
	}

	default @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
		return self().getParticleIcon();
	}

	default net.fabricmc.fabric.api.util.TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
		return net.fabricmc.fabric.api.util.TriState.DEFAULT;
	}

	default @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
		return ChunkRenderTypeSet.of(ItemBlockRenderTypes.getChunkRenderType(state));
	}

	default List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
		return List.of(ItemBlockRenderTypes.getRenderType(itemStack, fabulous));
	}

	default BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
		self().getTransforms().getTransform(transformType).apply(applyLeftHandTransform, poseStack);
		return self();
	}

	/** NeoForge's multi-pass item rendering; vanilla renders a single pass. */
	default List<net.minecraft.client.resources.model.BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
		return List.of((net.minecraft.client.resources.model.BakedModel) this);
	}
}
