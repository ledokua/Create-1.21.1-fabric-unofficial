package com.simibubi.create.foundation.mixin.compat.opac;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import xaero.pac.common.server.core.ServerCore;

/**
 * Open Parties and Claims, as its NeoForge build does: using blocks on a contraption that sits in someone's claim.
 */
@Mixin(ContraptionInteractionPacket.class)
public abstract class ContraptionInteractionPacketMixin {
	@Shadow
	@Final
	private InteractionHand hand;

	@Shadow
	@Final
	private int target;

	@Shadow
	@Final
	private BlockPos localPos;

	@Inject(method = "handle", remap = false, at = @At("HEAD"), cancellable = true)
	private void create$opacCheck(ServerPlayer player, CallbackInfo ci) {
		if (player == null)
			return;
		if (!ServerCore.isCreateContraptionInteractionPacketAllowed(target, hand, localPos, player))
			ci.cancel();
	}
}
