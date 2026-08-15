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
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's extended baked-model contract (data-aware, layer-aware quads), Create-owned and bridged onto
 * Fabric's renderer API: implementing classes only write the NeoForge-shaped methods; {@link #emitBlockQuads}
 * is provided (via {@link FabricModelBridge}).
 */
public interface IDynamicBakedModel extends BakedModel, FabricBakedModel {

	@Override
	default @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
		return getQuads(state, side, rand, ModelDataContext.current(), ModelDataContext.currentLayer());
	}

	@NotNull
	List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType);

	// ---- FRAPI ----

	@Override
	default boolean isVanillaAdapter() {
		return false;
	}

	@Override
	default void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
		FabricModelBridge.emitBlockQuads(this, blockView, state, pos, randomSupplier, context);
	}
}
