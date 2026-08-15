package com.simibubi.create.foundation.mixin.extension;

import com.simibubi.create.foundation.model.data.ModelData;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.minecraft.core.BlockPos;

/** Injected onto {@code BlockAndTintGetter}: NeoForge's per-position model data lookup, over Fabric's render data. */
public interface BlockAndTintGetterExtension {
	default ModelData getModelData(BlockPos pos) {
		Object data = ((FabricBlockView) this).getBlockEntityRenderData(pos);
		return data instanceof ModelData md ? md : ModelData.EMPTY;
	}
}
