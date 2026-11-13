package com.simibubi.create.foundation.mixin.compat.opac;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import xaero.pac.common.server.core.ServerCore;

/** Open Parties and Claims, as its NeoForge build does: open pipe ends placing or draining fluids in someone's claim. */
@Mixin(OpenEndedPipe.class)
public abstract class OpenEndedPipeMixin {
	@Shadow
	private Level world;
	@Shadow
	private BlockPos pos;
	@Shadow
	private BlockPos outputPos;

	@Inject(method = "provideFluidToSpace", cancellable = true, at = @At(value = "INVOKE", ordinal = 0,
		target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private void create$opacProvide(FluidStack fluid, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
		if (!ServerCore.canCreatePipeAffectBlock(world, pos, outputPos, simulate))
			cir.setReturnValue(false);
	}

	@Inject(method = "removeFluidFromSpace", cancellable = true, at = @At(value = "INVOKE", ordinal = 0,
		target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private void create$opacRemove(boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
		if (!ServerCore.canCreatePipeAffectBlock(world, pos, outputPos, simulate))
			cir.setReturnValue(FluidStack.EMPTY);
	}
}
