package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's {@code shouldCheckWeakPower}: lets blocks opt out of conducting weak redstone power in
 * {@code SignalGetter#getSignal} (a default method, hence the interface mixin).
 */
@Mixin(SignalGetter.class)
public interface LevelSignalHooksMixin {
	@WrapOperation(method = "getSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isRedstoneConductor(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean create$shouldCheckWeakPower(BlockState state, BlockGetter level, BlockPos pos, Operation<Boolean> original, BlockPos argPos, Direction direction) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "shouldCheckWeakPower", BlockState.class, SignalGetter.class, BlockPos.class, Direction.class))
			return state.getBlock().shouldCheckWeakPower(state, (SignalGetter) this, pos, direction);
		return original.call(state, level, pos);
	}
}
