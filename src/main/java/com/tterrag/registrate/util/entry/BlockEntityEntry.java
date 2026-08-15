package com.tterrag.registrate.util.entry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class BlockEntityEntry<T extends BlockEntity> extends RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> {

	public static <T extends BlockEntity> BlockEntityEntry<T> unresolved(ResourceLocation id) {
		return new BlockEntityEntry<>(id);
	}

	protected BlockEntityEntry(ResourceLocation id) {
		super(Registries.BLOCK_ENTITY_TYPE, id);
	}

	public T create(BlockPos pos, BlockState state) {
		return get().create(pos, state);
	}

	public boolean is(BlockEntity be) {
		return be != null && be.getType() == get();
	}

	public Optional<T> get(BlockGetter level, BlockPos pos) {
		return level.getBlockEntity(pos, get());
	}
}
