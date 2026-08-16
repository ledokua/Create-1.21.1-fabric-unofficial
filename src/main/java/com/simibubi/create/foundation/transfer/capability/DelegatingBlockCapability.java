package com.simibubi.create.foundation.transfer.capability;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** {@link BlockCapability} view over a Fabric {@link BlockApiLookup} (which is an interface we can't extend from outside). */
record DelegatingBlockCapability<T, C>(BlockApiLookup<T, C> lookup) implements BlockCapability<T, C> {

	@Override
	public BlockApiLookup<T, C> fabricLookup() {
		return lookup;
	}

	@Override
	public T find(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, C context) {
		return lookup.find(world, pos, state, blockEntity, context);
	}

	@Override
	public void registerSelf(BlockEntityType<?>... blockEntityTypes) {
		lookup.registerSelf(blockEntityTypes);
	}

	@Override
	public void registerForBlocks(BlockApiProvider<T, C> provider, Block... blocks) {
		lookup.registerForBlocks(provider, blocks);
	}

	@Override
	public void registerForBlockEntities(BlockEntityApiProvider<T, C> provider, BlockEntityType<?>... blockEntityTypes) {
		lookup.registerForBlockEntities(provider, blockEntityTypes);
	}

	@Override
	public void registerFallback(BlockApiProvider<T, C> fallbackProvider) {
		lookup.registerFallback(fallbackProvider);
	}

	@Override
	public ResourceLocation getId() {
		return lookup.getId();
	}

	@Override
	public Class<T> apiClass() {
		return lookup.apiClass();
	}

	@Override
	public Class<C> contextClass() {
		return lookup.contextClass();
	}

	@Override
	public BlockApiProvider<T, C> getProvider(Block block) {
		return lookup.getProvider(block);
	}
}
