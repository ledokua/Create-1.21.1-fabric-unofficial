package com.simibubi.create.foundation.extension;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Injected (generically) onto {@code BlockEntityRenderer<T>}: NeoForge's per-renderer culling box (vanilla 1.21 renders all block entities of visible sections, so this is only used by Create's own code; NeoForge would also frustum-cull with it). */
public interface BlockEntityRendererExtension<T extends BlockEntity> {

	default AABB getRenderBoundingBox(T blockEntity) {
		return new AABB(blockEntity.getBlockPos()).inflate(1);
	}
}
