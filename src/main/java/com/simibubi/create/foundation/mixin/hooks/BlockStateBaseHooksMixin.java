package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

/** Routes vanilla's per-state piston reaction through NeoForge's {@code IBlockExtension#getPistonPushReaction}. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseHooksMixin {
	@Shadow
	public abstract Block getBlock();

	@Inject(method = "getPistonPushReaction", at = @At("HEAD"), cancellable = true)
	private void create$pistonPushReaction(CallbackInfoReturnable<PushReaction> cir) {
		Block block = getBlock();
		if (!ExtensionOverrides.overrides(block, IBlockExtension.class, "getPistonPushReaction", BlockState.class))
			return;
		PushReaction reaction = block.getPistonPushReaction((BlockState) (Object) this);
		if (reaction != null)
			cir.setReturnValue(reaction);
	}
}
