package com.simibubi.create.foundation.extension;

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

	/** Mod-private NBT that persists with the block entity (NeoForge's {@code BlockEntity#getPersistentData()}). */
	default net.minecraft.nbt.CompoundTag getPersistentData() {
		throw new AbstractMethodError("BlockEntityExtension#getPersistentData not implemented by mixin");
	}

	/** Client: a block entity data packet arrived (NeoForge hook; vanilla loads the tag directly). Wired by mixin. */
	default void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, net.minecraft.core.HolderLookup.Provider registries) {
		net.minecraft.nbt.CompoundTag tag = pkt.getTag();
		if (tag != null)
			handleUpdateTag(tag, registries);
	}

	/** Client: chunk-load update tag arrived (NeoForge hook). Wired by mixin. */
	default void handleUpdateTag(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
		((net.minecraft.world.level.block.entity.BlockEntity) this).loadWithComponents(tag, registries);
	}

	/** Called once after the block entity's level is set and it is added to the level. Wired by mixin. */
	default void onLoad() {}

	/** Called when the chunk holding this block entity unloads. Wired by mixin. */
	default void onChunkUnloaded() {}
}
