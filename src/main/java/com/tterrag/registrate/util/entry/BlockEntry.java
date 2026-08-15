package com.tterrag.registrate.util.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntry<T extends Block> extends ItemProviderEntry<Block, T> {

	public static <T extends Block> BlockEntry<T> unresolved(ResourceLocation id) {
		return new BlockEntry<>(id);
	}

	protected BlockEntry(ResourceLocation id) {
		super(Registries.BLOCK, id);
	}

	public BlockState getDefaultState() {
		return get().defaultBlockState();
	}

	public boolean has(BlockState state) {
		return isPresent() && get() == state.getBlock();
	}
}
