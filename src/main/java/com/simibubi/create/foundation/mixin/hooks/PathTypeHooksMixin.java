package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/** NeoForge's {@code getBlockPathType} (belts/seats are RAIL for mob pathfinding). */
@Mixin(WalkNodeEvaluator.class)
public class PathTypeHooksMixin {
	@Inject(method = "getPathTypeFromState", at = @At("HEAD"), cancellable = true)
	private static void create$blockPathType(BlockGetter level, BlockPos pos, CallbackInfoReturnable<PathType> cir) {
		BlockState state = level.getBlockState(pos);
		if (!ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getBlockPathType", BlockState.class, BlockGetter.class, BlockPos.class, Mob.class))
			return;
		PathType type = state.getBlock().getBlockPathType(state, level, pos, null);
		if (type != null)
			cir.setReturnValue(type);
	}
}
