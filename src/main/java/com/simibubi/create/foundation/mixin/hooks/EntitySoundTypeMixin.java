package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge's {@code getSoundType(state, level, pos, entity)} for step sounds ({@code Entity#playStepSound}). */
@Mixin(Entity.class)
public abstract class EntitySoundTypeMixin {
	@WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
	private SoundType create$stepSound(BlockState state, Operation<SoundType> original, BlockPos pos, BlockState argState) {
		Entity self = (Entity) (Object) this;
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class))
			return state.getBlock().getSoundType(state, self.level(), pos, self);
		return original.call(state);
	}
}
