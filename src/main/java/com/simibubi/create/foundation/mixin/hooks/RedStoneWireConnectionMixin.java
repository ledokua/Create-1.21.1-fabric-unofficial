package com.simibubi.create.foundation.mixin.hooks;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's {@code canConnectRedstone}: redstone dust decides whether to visually/logically connect to a block
 * (links, contacts, latches, nixie tubes…). Vanilla's check is static and position-less; Create's overrides only use
 * state and side, so level/pos are passed as null.
 */
@Mixin(RedStoneWireBlock.class)
public class RedStoneWireConnectionMixin {
	@Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At("HEAD"), cancellable = true)
	private static void create$canConnectRedstone(BlockState state, @Nullable Direction direction, CallbackInfoReturnable<Boolean> cir) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "canConnectRedstone", BlockState.class, BlockGetter.class, BlockPos.class, Direction.class))
			cir.setReturnValue(state.getBlock().canConnectRedstone(state, null, null, direction));
	}
}
