package com.simibubi.create.foundation.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Entities that need extra data sent with their spawn packet (Create-owned; was NeoForge's). The server side
 * is a mixin on {@code ServerEntity#sendPairingData} appending a {@link ComplexSpawnPayload} right after the
 * vanilla add-entity packet; the client applies it in {@link ComplexSpawnPayload#handleClient}.
 */
public interface IEntityWithComplexSpawn {

	void writeSpawnData(RegistryFriendlyByteBuf buffer);

	void readSpawnData(RegistryFriendlyByteBuf additionalData);
}
