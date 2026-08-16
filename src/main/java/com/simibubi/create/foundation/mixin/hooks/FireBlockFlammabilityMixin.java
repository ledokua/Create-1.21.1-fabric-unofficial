package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's per-position flammability: vanilla FireBlock reads a static block→odds map; blocks overriding
 * {@code getFlammability}/{@code getFireSpreadSpeed} (cardboard, waterwheels, belts) get consulted instead.
 * Position/direction context isn't available in vanilla's odds getters, so those get nulls (Create's overrides don't use them).
 */
@Mixin(FireBlock.class)
public class FireBlockFlammabilityMixin {
	@Inject(method = "getBurnOdds", at = @At("HEAD"), cancellable = true)
	private void create$burnOdds(BlockState state, CallbackInfoReturnable<Integer> cir) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getFlammability", BlockState.class, BlockGetter.class, BlockPos.class, Direction.class))
			cir.setReturnValue(state.getBlock().getFlammability(state, null, null, null));
	}

	@Inject(method = "getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("HEAD"), cancellable = true)
	private void create$igniteOdds(BlockState state, CallbackInfoReturnable<Integer> cir) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getFireSpreadSpeed", BlockState.class, BlockGetter.class, BlockPos.class, Direction.class))
			cir.setReturnValue(state.getBlock().getFireSpreadSpeed(state, null, null, null));
	}
}
