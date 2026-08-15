package com.simibubi.create.foundation.entity;

import com.simibubi.create.Create;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;

/** Extra spawn data for an {@link IEntityWithComplexSpawn}; see there. */
public record ComplexSpawnPayload(int entityId, byte[] data) implements CustomPacketPayload {

	public static final Type<ComplexSpawnPayload> TYPE = new Type<>(Create.asResource("complex_spawn"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ComplexSpawnPayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT, ComplexSpawnPayload::entityId, ByteBufCodecs.BYTE_ARRAY, ComplexSpawnPayload::data, ComplexSpawnPayload::new);

	public static ComplexSpawnPayload of(Entity entity) {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), entity.registryAccess());
		try {
			((IEntityWithComplexSpawn) entity).writeSpawnData(buf);
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			return new ComplexSpawnPayload(entity.getId(), bytes);
		} finally {
			buf.release();
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Common init: register the payload type. */
	public static void register() {
		PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
	}

	/** Client init: apply incoming payloads to the (already spawned) entity. */
	@Environment(EnvType.CLIENT)
	public static void registerClientReceiver() {
		ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> payload.handleClient(context.client()));
	}

	@Environment(EnvType.CLIENT)
	private void handleClient(Minecraft mc) {
		if (mc.level == null)
			return;
		Entity entity = mc.level.getEntity(entityId);
		if (!(entity instanceof IEntityWithComplexSpawn complex))
			return;
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), mc.level.registryAccess());
		try {
			complex.readSpawnData(buf);
		} finally {
			buf.release();
		}
	}
}
