package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.platform.IBaseRailBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge's {@code BaseRailBlock#canMakeSlopes} (cart assemblers never become ascending rails). */
@Mixin(RailState.class)
public class RailStateSlopesMixin {
	@Shadow
	@Final
	private BlockPos pos;
	@Shadow
	@Final
	private BaseRailBlock block;
	@Shadow
	private BlockState state;

	@WrapOperation(method = { "connectTo", "place" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;isRail(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean create$canMakeSlopes(Level level, BlockPos checkPos, Operation<Boolean> original) {
		if (checkPos.getY() > pos.getY()
			&& ExtensionOverrides.overrides(block, IBaseRailBlockExtension.class, "canMakeSlopes", BlockState.class, BlockGetter.class, BlockPos.class)
			&& !block.canMakeSlopes(state, level, pos))
			return false;
		return original.call(level, checkPos);
	}
}
