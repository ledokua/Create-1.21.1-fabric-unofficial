package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** NeoForge's positional {@code getExplosionResistance(state, level, pos, explosion)} (copycats use their material's). */
@Mixin(ExplosionDamageCalculator.class)
public class ExplosionResistanceMixin {
	@WrapOperation(method = "getBlockExplosionResistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getExplosionResistance()F"))
	private float create$explosionResistance(Block block, Operation<Float> original, Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, FluidState fluid) {
		if (ExtensionOverrides.overrides(block, IBlockExtension.class, "getExplosionResistance", BlockState.class, BlockGetter.class, BlockPos.class, Explosion.class))
			return block.getExplosionResistance(state, level, pos, explosion);
		return original.call(block);
	}
}
