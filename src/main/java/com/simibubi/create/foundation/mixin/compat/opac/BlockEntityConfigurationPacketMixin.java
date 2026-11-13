package com.simibubi.create.foundation.mixin.compat.opac;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import xaero.pac.common.server.core.ServerCore;

/**
 * Open Parties and Claims, as its NeoForge build does: changing the settings of a Create block entity (value boxes,
 * filters, signals, ...) in someone's claim. Stock requests from a stock ticker stay allowed, as in OPAC.
 */
@Mixin(BlockEntityConfigurationPacket.class)
public abstract class BlockEntityConfigurationPacketMixin {
	@Shadow
	@Final
	protected BlockPos pos;

	@Inject(method = "handle", remap = false, at = @At("HEAD"), cancellable = true)
	private void create$opacCheck(ServerPlayer player, CallbackInfo ci) {
		if (player == null || (Object) this instanceof LogisticalStockRequestPacket)
			return;
		if (!ServerCore.isCreateTileEntityPacketAllowed(pos, player))
			ci.cancel();
	}
}
