package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;

/** NeoForge's position-aware {@code getLightEmission(state, level, pos)} in the block light engine (copycats, tanks). */
@Mixin(BlockLightEngine.class)
public abstract class BlockLightEmissionMixin {
	@WrapOperation(method = "getEmission", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
	private int create$lightEmission(BlockState state, Operation<Integer> original, long packedPos, BlockState argState) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getLightEmission", BlockState.class, BlockGetter.class, BlockPos.class)) {
			BlockGetter level = ((com.simibubi.create.foundation.extension.LightEngineLevelAccess) this).create$level();
			if (level != null)
				return state.getBlock().getLightEmission(state, level, BlockPos.of(packedPos));
		}
		return original.call(state);
	}
}
