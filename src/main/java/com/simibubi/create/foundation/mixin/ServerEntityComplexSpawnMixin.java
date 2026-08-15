package com.simibubi.create.foundation.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.entity.ComplexSpawnPayload;
import com.simibubi.create.foundation.entity.IEntityWithComplexSpawn;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Sends {@link ComplexSpawnPayload} right after the vanilla add-entity packet for {@link IEntityWithComplexSpawn}s. */
@Mixin(ServerEntity.class)
public class ServerEntityComplexSpawnMixin {

	@Shadow
	@Final
	private Entity entity;

	@SuppressWarnings("unchecked")
	@Inject(method = "sendPairingData", at = @At("TAIL"))
	private void create$sendComplexSpawnData(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> consumer, CallbackInfo ci) {
		if (entity instanceof IEntityWithComplexSpawn)
			consumer.accept((Packet<ClientGamePacketListener>) (Packet<?>) ServerPlayNetworking.createS2CPacket(ComplexSpawnPayload.of(entity)));
	}
}
