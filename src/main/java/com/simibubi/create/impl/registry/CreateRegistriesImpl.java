package com.simibubi.create.impl.registry;

import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.registry.CreateRegistries;

import com.simibubi.create.foundation.event.bus.SubscribeEvent;
import com.simibubi.create.foundation.event.bus.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber
public class CreateRegistriesImpl {
	@SubscribeEvent
	public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(
			CreateRegistries.POTATO_PROJECTILE_TYPE,
			PotatoCannonProjectileType.CODEC,
			PotatoCannonProjectileType.CODEC
		);
	}
}
