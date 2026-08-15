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

	/** Wraps an already-registered block (e.g. another mod's) in an entry; upstream builds these from a DeferredHolder. */
	public static <T extends Block> BlockEntry<T> of(T block) {
		BlockEntry<T> entry = new BlockEntry<>(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block));
		entry.resolve(block);
		return entry;
	}

	public BlockState getDefaultState() {
		return get().defaultBlockState();
	}

	public boolean has(BlockState state) {
		return is(state.getBlock());
	}

	/** Upstream {@code BlockEntry#is(Item)}: matches this block's item. */
	public boolean is(net.minecraft.world.item.Item item) {
		return isItem(item);
	}
}
