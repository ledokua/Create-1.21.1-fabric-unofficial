package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's {@code BaseRailBlock#onMinecartPass} (cart assemblers assemble/disassemble, controller rails act) — fired
 * once per {@code moveAlongTrack} for the rail the cart is on.
 */
@Mixin(AbstractMinecart.class)
public class MinecartRailHooksMixin {
	@Inject(method = "moveAlongTrack", at = @At("RETURN"))
	private void create$onMinecartPass(BlockPos pos, BlockState state, CallbackInfo ci) {
		AbstractMinecart self = (AbstractMinecart) (Object) this;
		if (state.getBlock() instanceof BaseRailBlock rail)
			rail.onMinecartPass(state, self.level(), pos, self);
	}
}
