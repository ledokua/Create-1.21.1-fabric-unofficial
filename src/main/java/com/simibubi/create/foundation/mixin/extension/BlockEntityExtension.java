package com.simibubi.create.foundation.mixin.extension;

/** Injected onto {@code net.minecraft.world.level.block.entity.BlockEntity}. */
public interface BlockEntityExtension {

	/**
	 * NeoForge notifies capability caches here. Fabric's {@code BlockApiCache} re-resolves on block-entity
	 * change by itself, so nothing to do — kept so upstream call sites compile.
	 */
	default void invalidateCapabilities() {}
}
