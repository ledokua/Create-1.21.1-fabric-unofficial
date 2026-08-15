package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.player.PlayerEvent;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** NeoForge's {@code PlayerEvent.StartTracking}: a player starts receiving updates for an entity. */
@Mixin(ChunkMap.TrackedEntity.class)
public class TrackedEntityStartTrackingMixin {
	@Shadow
	@Final
	Entity entity;

	@Inject(method = "updatePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity;addPairing(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
	private void create$startTracking(ServerPlayer player, CallbackInfo ci) {
		CreateEventBus.BUS.post(new PlayerEvent.StartTracking(player, entity));
	}
}
