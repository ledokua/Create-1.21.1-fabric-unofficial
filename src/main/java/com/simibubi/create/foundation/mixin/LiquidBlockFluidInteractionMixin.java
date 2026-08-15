package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.transfer.fluid.FluidInteractionRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Lets {@link FluidInteractionRegistry} interactions fire where NeoForge patches the same method. */
@Mixin(LiquidBlock.class)
public class LiquidBlockFluidInteractionMixin {

	@Inject(method = "shouldSpreadLiquid", at = @At("HEAD"), cancellable = true)
	private void create$fluidInteractions(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (FluidInteractionRegistry.canInteract(level, pos))
			cir.setReturnValue(false);
	}
}
