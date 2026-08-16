package com.simibubi.create.foundation.datamap;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.DataResult;
import com.simibubi.create.Create;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends the loaded {@link DataMaps} to clients (NeoForge syncs its data maps on login / reload). Layout:
 * {@code {<type id>: {<entry id>: <value NBT>}}}. Applied on the client only for remote servers (the integrated
 * server shares the static maps).
 */
public record DataMapSyncPayload(CompoundTag data) implements CustomPacketPayload {

	public static final Type<DataMapSyncPayload> TYPE = new Type<>(Create.asResource("data_map_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DataMapSyncPayload> CODEC =
		ByteBufCodecs.TRUSTED_COMPOUND_TAG.map(DataMapSyncPayload::new, DataMapSyncPayload::data).cast();

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	/** Common init. */
	public static void register() {
		PayloadTypeRegistry.playS2C().register(TYPE, CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sender.sendPacket(build()));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
			if (!success)
				return;
			DataMapSyncPayload payload = build();
			for (ServerPlayer player : server.getPlayerList().getPlayers())
				ServerPlayNetworking.send(player, payload);
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static DataMapSyncPayload build() {
		CompoundTag root = new CompoundTag();
		for (DataMapType<?, ?> type : DataMaps.types()) {
			Registry registry = DataMaps.registryOf(type);
			if (registry == null)
				continue;
			CompoundTag entries = new CompoundTag();
			DataMaps.view((DataMapType) type).forEach((entry, value) -> {
				ResourceLocation key = registry.getKey(entry);
				if (key == null)
					return;
				DataResult<Tag> encoded = ((DataMapType<Object, Object>) type).codec().encodeStart(NbtOps.INSTANCE, value);
				encoded.resultOrPartial(err -> Create.LOGGER.error("Failed to encode data map {} value for {}: {}", type, key, err))
					.ifPresent(tag -> entries.put(key.toString(), tag));
			});
			root.put(type.id().toString(), entries);
		}
		return new DataMapSyncPayload(root);
	}

	/** Client init. */
	@Environment(EnvType.CLIENT)
	public static void registerClientReceiver() {
		ClientPlayNetworking.registerGlobalReceiver(TYPE, (payload, context) -> {
			if (Minecraft.getInstance().hasSingleplayerServer())
				return;
			payload.apply();
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void apply() {
		for (DataMapType<?, ?> type : DataMaps.types()) {
			CompoundTag entries = data.getCompound(type.id().toString());
			Registry registry = DataMaps.registryOf(type);
			if (registry == null)
				continue;
			Map<Object, Object> values = new IdentityHashMap<>();
			for (String key : entries.getAllKeys()) {
				Optional<?> entry = registry.getOptional(ResourceLocation.parse(key));
				if (entry.isEmpty())
					continue;
				((DataMapType<Object, Object>) type).codec().parse(NbtOps.INSTANCE, entries.get(key))
					.resultOrPartial(err -> Create.LOGGER.error("Failed to decode synced data map {} value for {}: {}", type, key, err))
					.ifPresent(value -> values.put(entry.get(), value));
			}
			DataMaps.set((DataMapType) type, values);
		}
	}
}
