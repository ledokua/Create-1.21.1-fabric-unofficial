package com.simibubi.create.foundation.mixin.hooks.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.client.extensions.IClientBlockExtensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** NeoForge's {@code IClientBlockExtensions#addHitEffects} ({@code ParticleEngine#addBlockHitEffects} in {@code Minecraft#continueAttack}). */
@Mixin(Minecraft.class)
public class BlockHitEffectsMixin {
	@WrapOperation(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;crack(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V"))
	private void create$addHitEffects(ParticleEngine engine, BlockPos pos, Direction side, Operation<Void> original) {
		Minecraft mc = (Minecraft) (Object) this;
		BlockState state = mc.level.getBlockState(pos);
		HitResult hit = mc.hitResult instanceof BlockHitResult bhr ? bhr : new BlockHitResult(Vec3.atCenterOf(pos), side, pos, false);
		if (IClientBlockExtensions.of(state).addHitEffects(state, mc.level, hit, engine))
			return;
		original.call(engine, pos, side);
	}
}
