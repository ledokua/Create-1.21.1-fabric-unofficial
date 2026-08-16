package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge's {@code isScaffolding} (metal scaffolding: sneaking doesn't stop the slide down like on ladders). */
@Mixin(LivingEntity.class)
public class LivingEntityScaffoldingMixin {
	@WrapOperation(method = "handleOnClimbable", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
	private boolean create$isScaffolding(BlockState state, Block block, Operation<Boolean> original) {
		if (original.call(state, block))
			return true;
		LivingEntity self = (LivingEntity) (Object) this;
		return ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "isScaffolding", BlockState.class, LevelReader.class, BlockPos.class, LivingEntity.class)
			&& state.getBlock().isScaffolding(state, self.level(), self.blockPosition(), self);
	}
}
