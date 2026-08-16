package com.simibubi.create.foundation.extension;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/** Injected onto {@code ChunkAccess}: NeoForge's two-stage {@code findBlocks}. */
public interface ChunkAccessExtension {

	default void findBlocks(Predicate<BlockState> roughFilter, BiPredicate<BlockState, BlockPos> fineFilter, BiConsumer<BlockPos, BlockState> output) {
		((ChunkAccess) this).findBlocks(state -> roughFilter.test(state) && true, (pos, state) -> {
			if (fineFilter.test(state, pos))
				output.accept(pos, state);
		});
	}
}
