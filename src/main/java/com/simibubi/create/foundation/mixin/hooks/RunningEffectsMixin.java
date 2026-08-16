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

/** NeoForge's {@code addRunningEffects} (waterwheels, stickers, fake tracks, copycats). */
@Mixin(Entity.class)
public class RunningEffectsMixin {
	@WrapOperation(method = "spawnSprintParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
	private void create$addRunningEffects(Level level, ParticleOptions options, double x, double y, double z, double dx, double dy, double dz,
		Operation<Void> original, @Local(ordinal = 0) BlockPos pos, @Local BlockState state) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "addRunningEffects", BlockState.class, Level.class, BlockPos.class, Entity.class)
			&& state.getBlock().addRunningEffects(state, level, pos, (Entity) (Object) this))
			return;
		original.call(level, options, x, y, z, dx, dy, dz);
	}
}
