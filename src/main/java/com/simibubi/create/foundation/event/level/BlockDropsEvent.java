package com.simibubi.create.foundation.event.level;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Fired when a broken block's drops are about to spawn; handlers may edit them or cancel. */
public class BlockDropsEvent extends BlockEvent implements ICancellableEvent {

	@Nullable
	private final BlockEntity blockEntity;
	private final List<ItemEntity> drops;
	@Nullable
	private final Entity breaker;
	private final ItemStack tool;
	private int experience;

	public BlockDropsEvent(ServerLevel level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, List<ItemEntity> drops, @Nullable Entity breaker, ItemStack tool) {
		super(level, pos, state);
		this.blockEntity = blockEntity;
		this.drops = drops;
		this.breaker = breaker;
		this.tool = tool;
	}

	@Override
	public ServerLevel getLevel() {
		return (ServerLevel) super.getLevel();
	}

	@Nullable
	public BlockEntity getBlockEntity() {
		return blockEntity;
	}

	public List<ItemEntity> getDrops() {
		return drops;
	}

	@Nullable
	public Entity getBreaker() {
		return breaker;
	}

	public ItemStack getTool() {
		return tool;
	}

	public int getDroppedExperience() {
		return experience;
	}

	public void setDroppedExperience(int experience) {
		this.experience = experience;
	}
}
