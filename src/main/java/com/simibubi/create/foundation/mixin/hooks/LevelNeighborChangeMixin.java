package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's {@code onNeighborChange} (comparator-style "output signal" notifications; packagers watch the block
 * behind them). Fired from {@code Level#updateNeighbourForOutputSignal} where vanilla only notifies comparators.
 */
@Mixin(Level.class)
public class LevelNeighborChangeMixin {
	@WrapOperation(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
	private boolean create$onNeighborChange(BlockState state, Block comparator, Operation<Boolean> original,
		BlockPos pos, Block block, @Local(ordinal = 1) BlockPos neighborPos) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "onNeighborChange", BlockState.class, LevelReader.class, BlockPos.class, BlockPos.class))
			state.getBlock().onNeighborChange(state, (Level) (Object) this, neighborPos, pos);
		return original.call(state, comparator);
	}
}
