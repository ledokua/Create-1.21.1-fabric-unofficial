package com.simibubi.create;

import com.simibubi.create.content.trains.entity.CarriageSyncDataSerializer;

import com.simibubi.create.foundation.event.bus.IEventBus;
import net.minecraft.network.syncher.EntityDataSerializers;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllEntityDataSerializers {
	// NeoForge has a registry for these (NeoForgeRegistries.ENTITY_DATA_SERIALIZERS); vanilla/Fabric use the static id map.
	public static final CarriageSyncDataSerializer CARRIAGE_DATA = new CarriageSyncDataSerializer();

	@Internal
	public static void register(IEventBus modEventBus) {
		EntityDataSerializers.registerSerializer(CARRIAGE_DATA);
	}
}
