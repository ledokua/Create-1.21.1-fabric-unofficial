package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.foundation.mixin.extension.BlockEntityExtension;
import com.simibubi.create.foundation.model.data.ModelData;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Exposes {@link BlockEntityExtension#getModelData()} as Fabric render data so models can read it. */
@Mixin(BlockEntity.class)
public class BlockEntityExtensionMixin implements BlockEntityExtension, RenderDataBlockEntity {

	@Override
	public Object getRenderData() {
		ModelData data = getModelData();
		return data == ModelData.EMPTY ? null : data;
	}
}
