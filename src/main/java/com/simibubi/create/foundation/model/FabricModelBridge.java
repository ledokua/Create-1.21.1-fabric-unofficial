package com.simibubi.create.foundation.model;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.model.data.ModelData;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glue between the NeoForge-shaped model API Create's custom models implement ({@code
 * getQuads(state, side, rand, ModelData, RenderType)}, {@code getModelData(...)}, {@code getRenderTypes(...)})
 * and Fabric's renderer API: {@link #emitBlockQuads} asks the model for its quads per render layer and feeds
 * them to the FRAPI {@link QuadEmitter} with the matching {@link BlendMode}. See PORTING_PLAN.md,
 * "NeoForge → Fabric translation strategy" (client model data bucket).
 */
public final class FabricModelBridge {

	private FabricModelBridge() {}

	private static final Direction[] SIDES_AND_NULL = { null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };
	private static final int FULL_BRIGHT = 0xF000F0;

	/** The {@link ModelData} a level exposes at a position (a Create block entity's render data), or EMPTY. */
	public static ModelData modelDataAt(BlockAndTintGetter level, BlockPos pos) {
		Object data = ((FabricBlockView) level).getBlockEntityRenderData(pos);
		return data instanceof ModelData md ? md : ModelData.EMPTY;
	}

	public static void emitBlockQuads(IDynamicBakedModel model, BlockAndTintGetter level, BlockState state, BlockPos pos,
		Supplier<RandomSource> randomSupplier, RenderContext context) {
		ModelData data = model.getModelData(level, pos, state, modelDataAt(level, pos));
		ChunkRenderTypeSet renderTypes = model.getRenderTypes(state, randomSupplier.get(), data);
		QuadEmitter emitter = context.getEmitter();
		for (RenderType renderType : renderTypes) {
			RenderMaterial material = material(renderType, false);
			RenderMaterial emissive = null;
			for (Direction side : SIDES_AND_NULL) {
				if (side != null && context.isFaceCulled(side))
					continue;
				List<BakedQuad> quads = model.getQuads(state, side, randomSupplier.get(), data, renderType);
				for (BakedQuad quad : quads) {
					boolean fullBright = isFullBright(quad);
					if (fullBright && emissive == null)
						emissive = material(renderType, true);
					emitter.fromVanilla(quad, fullBright ? emissive : material, side);
					if (fullBright)
						emitter.lightmap(FULL_BRIGHT, FULL_BRIGHT, FULL_BRIGHT, FULL_BRIGHT);
					emitter.emit();
				}
			}
		}
	}

	private static RenderMaterial material(RenderType renderType, boolean emissive) {
		return RendererAccess.INSTANCE.getRenderer().materialFinder()
			.blendMode(BlendMode.fromRenderLayer(renderType))
			.emissive(emissive)
			.find();
	}

	/** True if {@link QuadTransformers#settingMaxEmissivity()} (or upstream data) baked full-bright lightmaps into the quad. */
	static boolean isFullBright(BakedQuad quad) {
		int[] v = quad.getVertices();
		int stride = v.length / 4;
		if (stride < 7)
			return false;
		for (int i = 0; i < 4; i++)
			if (v[i * stride + 6] != FULL_BRIGHT)
				return false;
		return true;
	}

	/** Vanilla-shaped fallback: quads for the given side across all layers, used by the 3-arg getQuads. */
	public static List<BakedQuad> getQuadsAllLayers(IDynamicBakedModel model, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data) {
		return model.getQuads(state, side, rand, data, null);
	}

	public static int overlay() {
		return OverlayTexture.NO_OVERLAY;
	}
}
