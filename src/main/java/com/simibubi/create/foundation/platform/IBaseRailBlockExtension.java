package com.simibubi.create.foundation.platform;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * NeoForge's rail block extension (Create-owned): implemented by vanilla rails via interface injection and
 * by Create's track blocks directly. Only the members Create uses.
 */
public interface IBaseRailBlockExtension {

	RailShape getRailDirection(BlockState state, BlockGetter level, BlockPos pos, @Nullable AbstractMinecart cart);

	default boolean isFlexibleRail(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	default boolean canMakeSlopes(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	default float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
		return 0.4F;
	}

	default void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {}
}
