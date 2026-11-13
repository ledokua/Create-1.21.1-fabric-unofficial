package com.simibubi.create.foundation.mixin.compat.opac;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.equipment.clipboard.ClipboardEditPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import xaero.pac.common.server.core.ServerCore;

/** Open Parties and Claims, as its NeoForge build does: writing a clipboard into a block in someone's claim. */
@Mixin(ClipboardEditPacket.class)
public abstract class ClipboardEditPacketMixin {
	@Shadow
	@Final
	private BlockPos targetedBlock;

	@Inject(method = "handle", remap = false, at = @At("HEAD"), cancellable = true)
	private void create$opacCheck(ServerPlayer player, CallbackInfo ci) {
		if (player == null || targetedBlock == null)
			return;
		if (!ServerCore.isCreateTileEntityPacketAllowed(targetedBlock, player))
			ci.cancel();
	}
}
