package com.simibubi.create.foundation.mixin.extension;

import com.simibubi.create.foundation.model.data.ModelData;

/** Injected onto {@code net.minecraft.world.level.block.entity.BlockEntity}. */
public interface BlockEntityExtension {

	/**
	 * NeoForge notifies capability caches here. Fabric's {@code BlockApiCache} re-resolves on block-entity
	 * change by itself, so nothing to do — kept so upstream call sites compile.
	 */
	default void invalidateCapabilities() {}

	/** Per-position model data for this block entity's model; exposed to Fabric as render data by the mixin. */
	default ModelData getModelData() {
		return ModelData.EMPTY;
	}

	/** NeoForge: re-query model data + rebuild chunk. Fabric re-reads render data on chunk rebuild anyway. */
	default void requestModelDataUpdate() {}
}
