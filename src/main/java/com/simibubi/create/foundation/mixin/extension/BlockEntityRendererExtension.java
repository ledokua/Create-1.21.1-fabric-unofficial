package com.simibubi.create.foundation.mixin.extension;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Injected onto {@code BlockEntityRenderer}: NeoForge's per-renderer culling box (vanilla uses a fixed one). TODO(Phase 3): consult from LevelRenderer. */
public interface BlockEntityRendererExtension {

	default AABB getRenderBoundingBox(BlockEntity blockEntity) {
		return new AABB(blockEntity.getBlockPos()).inflate(1);
	}
}
