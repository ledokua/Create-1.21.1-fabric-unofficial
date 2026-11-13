package com.simibubi.create.foundation.mixin.compat.opac;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.contraptions.glue.SuperGlueRemovalPacket;

import net.minecraft.server.level.ServerPlayer;

import xaero.pac.common.server.core.ServerCore;

/** Open Parties and Claims, as its NeoForge build does: removing super glue in someone's claim. */
@Mixin(SuperGlueRemovalPacket.class)
public abstract class SuperGlueRemovalPacketMixin {
	@Shadow
	@Final
	private int entityId;

	@Inject(method = "handle", remap = false, at = @At("HEAD"), cancellable = true)
	private void create$opacCheck(ServerPlayer player, CallbackInfo ci) {
		if (player == null)
			return;
		if (!ServerCore.isCreateGlueRemovalAllowed(entityId, player))
			ci.cancel();
	}
}
