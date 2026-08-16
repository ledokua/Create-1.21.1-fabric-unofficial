package com.simibubi.create.foundation.mixin.hooks.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.client.extensions.IClientBlockExtensions;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge's {@code IClientBlockExtensions#addDestroyEffects} (block break particles). */
@Mixin(ParticleEngine.class)
public class BlockParticleEffectsMixin {
	@org.spongepowered.asm.mixin.Shadow
	protected ClientLevel level;

	@Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
	private void create$addDestroyEffects(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (!state.isAir() && IClientBlockExtensions.of(state).addDestroyEffects(state, level, pos, (ParticleEngine) (Object) this))
			ci.cancel();
	}
}
