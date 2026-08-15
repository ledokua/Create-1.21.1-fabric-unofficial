package com.simibubi.create.foundation.model;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.model.data.ModelData;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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
 * Delegating {@link BakedModel} base with NeoForge's data-aware overloads, bridged onto Fabric's renderer
 * API (see {@link IDynamicBakedModel}). Chains correctly: if the wrapped model is itself data-aware
 * (another wrapper or an {@link IDynamicBakedModel}) the data/layer-aware calls are forwarded as such;
 * plain vanilla models get the vanilla calls.
 */
public class BakedModelWrapper<T extends BakedModel> implements IDynamicBakedModel {

	protected final T originalModel;

	public BakedModelWrapper(T originalModel) {
		this.originalModel = originalModel;
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
		return originalModel.getQuads(state, side, rand);
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
		if (originalModel instanceof IDynamicBakedModel dyn)
			return dyn.getQuads(state, side, rand, extraData, renderType);
		return originalModel.getQuads(state, side, rand);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return originalModel.useAmbientOcclusion();
	}

	@Override
	public net.fabricmc.fabric.api.util.TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
		return originalModel.useAmbientOcclusion(state, data, renderType);
	}

	@Override
	public boolean isGui3d() {
		return originalModel.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return originalModel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return originalModel.isCustomRenderer();
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon() {
		return originalModel.getParticleIcon();
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
		if (originalModel instanceof IDynamicBakedModel dyn)
			return dyn.getParticleIcon(data);
		return originalModel.getParticleIcon();
	}

	@Override
	public @NotNull ItemTransforms getTransforms() {
		return originalModel.getTransforms();
	}

	@Override
	public @NotNull ItemOverrides getOverrides() {
		return originalModel.getOverrides();
	}

	@Override
	public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
		if (originalModel instanceof IDynamicBakedModel dyn)
			return dyn.getModelData(level, pos, state, modelData);
		return modelData;
	}

	@Override
	public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
		if (originalModel instanceof IDynamicBakedModel dyn)
			return dyn.getRenderTypes(state, rand, data);
		return IDynamicBakedModel.super.getRenderTypes(state, rand, data);
	}

	@Override
	public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
		if (originalModel instanceof IDynamicBakedModel dyn)
			return dyn.getRenderTypes(itemStack, fabulous);
		return IDynamicBakedModel.super.getRenderTypes(itemStack, fabulous);
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
		if (originalModel instanceof IDynamicBakedModel dyn)
			return dyn.applyTransform(transformType, poseStack, applyLeftHandTransform);
		return IDynamicBakedModel.super.applyTransform(transformType, poseStack, applyLeftHandTransform);
	}

	// ---- FRAPI: item quads just pass through; block quads go through the data-aware bridge ----

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<RandomSource> randomSupplier, RenderContext context) {
		if (originalModel instanceof FabricBakedModel fabric && !fabric.isVanillaAdapter())
			fabric.emitItemQuads(stack, randomSupplier, context);
		else
			context.bakedModelConsumer().accept(originalModel, null);
	}
}
