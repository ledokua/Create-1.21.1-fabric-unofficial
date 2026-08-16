package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * NeoForge's context-aware {@code getFriction(state, level, pos, entity)}: vanilla reads {@code Block#getFriction()}
 * in {@code LivingEntity#travel}. (Sound type / light emission use their own mixins.)
 */
@Mixin(LivingEntity.class)
public abstract class BlockSoundFrictionLightMixin {
	@WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
	private float create$friction(Block block, Operation<Float> original, Vec3 travelVector) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (ExtensionOverrides.overrides(block, IBlockExtension.class, "getFriction", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
			BlockPos pos = self.getBlockPosBelowThatAffectsMyMovement();
			return block.getFriction(self.level().getBlockState(pos), self.level(), pos, self);
		}
		return original.call(block);
	}
}
