package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge's {@code addLandingEffects} (waterwheels, stickers, fake tracks, copycats). */
@Mixin(LivingEntity.class)
public class LandingEffectsMixin {
	@WrapOperation(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
	private int create$addLandingEffects(ServerLevel level, ParticleOptions options, double x, double y, double z, int count, double dx, double dy, double dz, double speed,
		Operation<Integer> original, double fallDistance, boolean onGround, BlockState state, BlockPos pos) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "addLandingEffects", BlockState.class, ServerLevel.class, BlockPos.class, BlockState.class, LivingEntity.class, int.class)
			&& state.getBlock().addLandingEffects(state, level, pos, state, (LivingEntity) (Object) this, count))
			return 0;
		return original.call(level, options, x, y, z, count, dx, dy, dz, speed);
	}
}
