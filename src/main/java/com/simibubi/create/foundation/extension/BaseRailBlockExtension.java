package com.simibubi.create.foundation.extension;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.platform.IBaseRailBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

/** Injected onto {@code BaseRailBlock}: NeoForge's rail extension methods with vanilla behaviour. */
public interface BaseRailBlockExtension extends IBaseRailBlockExtension {

	@Override
	default RailShape getRailDirection(BlockState state, BlockGetter level, BlockPos pos, @Nullable AbstractMinecart cart) {
		return state.getValue(((BaseRailBlock) this).getShapeProperty());
	}
}
