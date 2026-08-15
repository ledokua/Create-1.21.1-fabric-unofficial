package com.simibubi.create.foundation.model;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * In-place vertex-data transforms on baked quads (subset of NeoForge's). Full-bright lightmaps written by
 * {@link #settingMaxEmissivity()} are detected by {@link FabricModelBridge} and emitted as emissive quads.
 */
public final class QuadTransformers {

	private QuadTransformers() {}

	private static final IQuadTransformer EMPTY = quad -> {};
	private static final IQuadTransformer MAX_EMISSIVE = settingEmissivity(15);

	public static IQuadTransformer empty() {
		return EMPTY;
	}

	public static IQuadTransformer settingMaxEmissivity() {
		return MAX_EMISSIVE;
	}

	public static IQuadTransformer settingEmissivity(int emissivity) {
		return settingEmissivity(emissivity, emissivity);
	}

	public static IQuadTransformer settingEmissivity(int blockLight, int skyLight) {
		int packed = (skyLight << 20) | (blockLight << 4);
		return quad -> {
			int[] v = quad.getVertices();
			int stride = v.length / 4;
			if (stride < 7)
				return;
			for (int i = 0; i < 4; i++)
				v[i * stride + 6] = packed;
		};
	}

	@FunctionalInterface
	public interface IQuadTransformer {
		void processInPlace(BakedQuad quad);

		default void processInPlace(List<BakedQuad> quads) {
			for (BakedQuad quad : quads)
				processInPlace(quad);
		}

		default BakedQuad process(BakedQuad quad) {
			BakedQuad copy = new BakedQuad(quad.getVertices().clone(), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
			processInPlace(copy);
			return copy;
		}

		default List<BakedQuad> process(List<BakedQuad> quads) {
			return quads.stream().map(this::process).toList();
		}
	}
}
