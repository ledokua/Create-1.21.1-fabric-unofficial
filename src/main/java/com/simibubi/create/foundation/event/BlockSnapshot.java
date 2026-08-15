package com.simibubi.create.foundation.event;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A captured block state + block entity NBT at a position, restorable later — what upstream's placement code
 * uses to undo a placement when {@code EntityPlaceEvent} is cancelled. Create-owned (was NeoForge's).
 */
public class BlockSnapshot {

	private final ResourceKey<Level> dim;
	private final BlockPos pos;
	private final int flags;
	private final BlockState state;
	@Nullable
	private final CompoundTag nbt;
	private final LevelAccessor level;

	private BlockSnapshot(ResourceKey<Level> dim, LevelAccessor level, BlockPos pos, BlockState state, @Nullable CompoundTag nbt, int flags) {
		this.dim = dim;
		this.pos = pos.immutable();
		this.state = state;
		this.flags = flags;
		this.nbt = nbt;
		this.level = level;
	}

	public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor level, BlockPos pos) {
		return create(dim, level, pos, Block.UPDATE_ALL);
	}

	public static BlockSnapshot create(ResourceKey<Level> dim, LevelAccessor level, BlockPos pos, int flags) {
		BlockEntity be = level.getBlockEntity(pos);
		CompoundTag nbt = be == null ? null : be.saveWithFullMetadata(level.registryAccess());
		return new BlockSnapshot(dim, level, pos, level.getBlockState(pos), nbt, flags);
	}

	public ResourceKey<Level> getDimension() {
		return dim;
	}

	public BlockPos getPos() {
		return pos;
	}

	public int getFlags() {
		return flags;
	}

	@Nullable
	public CompoundTag getTag() {
		return nbt;
	}

	public BlockState getState() {
		return state;
	}

	public LevelAccessor getLevel() {
		return level;
	}

	public BlockState getCurrentBlock() {
		return level.getBlockState(pos);
	}

	@Nullable
	public BlockEntity recreateBlockEntity(HolderLookup.Provider provider) {
		return nbt == null ? null : BlockEntity.loadStatic(pos, state, nbt, provider);
	}

	public boolean restore() {
		return restore(false);
	}

	public boolean restore(boolean force) {
		return restore(force, true);
	}

	public boolean restore(boolean force, boolean notifyNeighbors) {
		return restoreToLocation(level, pos, force, notifyNeighbors);
	}

	public boolean restore(int flags) {
		return restoreToLocation(level, pos, flags);
	}

	public boolean restoreToLocation(LevelAccessor level, BlockPos pos, boolean force, boolean notifyNeighbors) {
		return restoreToLocation(level, pos, notifyNeighbors ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS);
	}

	public boolean restoreToLocation(LevelAccessor level, BlockPos pos, int flags) {
		BlockState current = getCurrentBlock();
		if (current != state) {
			level.setBlock(pos, state, flags);
		}
		BlockEntity be = null;
		if (nbt != null) {
			be = level.getBlockEntity(pos);
			if (be != null) {
				be.loadWithComponents(nbt, level.registryAccess());
				be.setChanged();
			}
		}
		if (level instanceof Level l)
			l.sendBlockUpdated(pos, current, state, flags);
		return true;
	}
}
