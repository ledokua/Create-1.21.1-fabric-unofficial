package com.simibubi.create.foundation.event.level;

import com.simibubi.create.foundation.event.bus.Event;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/** Block change events. Fired from Fabric's PlayerBlockBreakEvents / a placement mixin (see CreateEventHooks). */
public abstract class BlockEvent extends Event {

	private final LevelAccessor level;
	private final BlockPos pos;
	private final BlockState state;

	protected BlockEvent(LevelAccessor level, BlockPos pos, BlockState state) {
		this.level = level;
		this.pos = pos;
		this.state = state;
	}

	public LevelAccessor getLevel() {
		return level;
	}

	public BlockPos getPos() {
		return pos;
	}

	public BlockState getState() {
		return state;
	}

	/** A player is about to break a block; cancellable. */
	public static class BreakEvent extends BlockEvent implements ICancellableEvent {
		private final Player player;

		public BreakEvent(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
			super(level, pos, state);
			this.player = player;
		}

		public Player getPlayer() {
			return player;
		}
	}

	/** An entity placed a block; cancelling reverts the placement. */
	public static class EntityPlaceEvent extends BlockEvent implements ICancellableEvent {
		private final Entity entity;
		private final BlockState placedBlock;
		private final BlockState placedAgainst;
		private final BlockState blockSnapshotState;

		public EntityPlaceEvent(LevelAccessor level, BlockPos pos, BlockState replacedState, BlockState placedBlock, BlockState placedAgainst, Entity entity) {
			super(level, pos, replacedState);
			this.entity = entity;
			this.placedBlock = placedBlock;
			this.placedAgainst = placedAgainst;
			this.blockSnapshotState = replacedState;
		}

		public Entity getEntity() {
			return entity;
		}

		public BlockState getPlacedBlock() {
			return placedBlock;
		}

		public BlockState getPlacedAgainst() {
			return placedAgainst;
		}

		/** The state that was replaced (upstream exposes a BlockSnapshot; Create only reads its state). */
		public BlockState getBlockSnapshotState() {
			return blockSnapshotState;
		}
	}
}
