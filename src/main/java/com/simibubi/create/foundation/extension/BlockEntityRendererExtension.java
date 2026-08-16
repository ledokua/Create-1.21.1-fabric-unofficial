package com.simibubi.create.foundation.extension;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

/** Injected (generically) onto {@code BlockEntityRenderer<T>}: NeoForge's per-renderer culling box. TODO(Phase 3): consult from LevelRenderer. */
public interface BlockEntityRendererExtension<T extends BlockEntity> {

	default AABB getRenderBoundingBox(T blockEntity) {
		return new AABB(blockEntity.getBlockPos()).inflate(1);
	}
}
