package com.simibubi.create.foundation.mixin.extension;

import com.simibubi.create.foundation.platform.AuxiliaryLightManager;

import net.minecraft.core.BlockPos;

/** Injected onto {@code LevelReader}: NeoForge's auxiliary light manager accessor (see {@link AuxiliaryLightManager}). */
public interface LevelExtension {
	default AuxiliaryLightManager getAuxLightManager(BlockPos pos) {
		return AuxiliaryLightManager.NONE;
	}

	/** NeoForge's {@code Level#invalidateCapabilities(pos)}; Fabric caches re-resolve on their own. No-op. */
	default void invalidateCapabilities(BlockPos pos) {}

	/**
	 * NeoForge's {@code Level#markAndNotifyBlock}: the "after a block was set" half of {@code Level#setBlock},
	 * for callers that placed the state through the chunk directly. Same sequence as vanilla's setBlock tail.
	 */
	default void markAndNotifyBlock(BlockPos pos, @org.jetbrains.annotations.Nullable net.minecraft.world.level.chunk.LevelChunk chunk,
		net.minecraft.world.level.block.state.BlockState oldState, net.minecraft.world.level.block.state.BlockState newState, int flags, int recursionLeft) {
		net.minecraft.world.level.Level level = (net.minecraft.world.level.Level) this;
		net.minecraft.world.level.block.state.BlockState current = level.getBlockState(pos);
		if (current != newState)
			return;
		if ((flags & net.minecraft.world.level.block.Block.UPDATE_CLIENTS) != 0 && (!level.isClientSide || (flags & net.minecraft.world.level.block.Block.UPDATE_INVISIBLE) == 0)
			&& (level.isClientSide || chunk == null || chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(net.minecraft.server.level.FullChunkStatus.BLOCK_TICKING)))
			level.sendBlockUpdated(pos, oldState, newState, flags);
		if ((flags & net.minecraft.world.level.block.Block.UPDATE_NEIGHBORS) != 0) {
			level.blockUpdated(pos, oldState.getBlock());
			if (!level.isClientSide && newState.hasAnalogOutputSignal())
				level.updateNeighbourForOutputSignal(pos, newState.getBlock());
		}
		if ((flags & net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE) == 0 && recursionLeft > 0) {
			int newFlags = flags & ~(net.minecraft.world.level.block.Block.UPDATE_NEIGHBORS | net.minecraft.world.level.block.Block.UPDATE_SUPPRESS_DROPS);
			oldState.updateIndirectNeighbourShapes(level, pos, newFlags, recursionLeft - 1);
			newState.updateNeighbourShapes(level, pos, newFlags, recursionLeft - 1);
			newState.updateIndirectNeighbourShapes(level, pos, newFlags, recursionLeft - 1);
		}
		level.onBlockStateChange(pos, oldState, newState);
	}
}
