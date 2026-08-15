package com.simibubi.create.foundation.model;

import com.simibubi.create.foundation.model.data.ModelData;

/**
 * Thread-local "model data for the block being tesselated right now". Vanilla's {@code ModelBlockRenderer}
 * only calls the data-less {@code getQuads(state, side, rand)}; NeoForge threads ModelData/RenderType through
 * an extra overload. Create's data-aware models read this in their vanilla getQuads path so the
 * NeoForge-shaped {@code tesselateBlock(..., ModelData, RenderType)} (see ModelBlockRendererExtension) works.
 */
public final class ModelDataContext {

	private ModelDataContext() {}

	private static final ThreadLocal<ModelData> DATA = ThreadLocal.withInitial(() -> ModelData.EMPTY);
	private static final ThreadLocal<net.minecraft.client.renderer.RenderType> LAYER = new ThreadLocal<>();

	public static ModelData current() {
		return DATA.get();
	}

	@org.jetbrains.annotations.Nullable
	public static net.minecraft.client.renderer.RenderType currentLayer() {
		return LAYER.get();
	}

	/** Runs {@code action} with the given data/layer as the ambient context. */
	public static void with(ModelData data, @org.jetbrains.annotations.Nullable net.minecraft.client.renderer.RenderType layer, Runnable action) {
		ModelData prevData = DATA.get();
		net.minecraft.client.renderer.RenderType prevLayer = LAYER.get();
		DATA.set(data);
		LAYER.set(layer);
		try {
			action.run();
		} finally {
			DATA.set(prevData);
			LAYER.set(prevLayer);
		}
	}
}
