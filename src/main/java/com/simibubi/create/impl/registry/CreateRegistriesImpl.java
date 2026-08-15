package com.simibubi.create.impl.registry;

import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.registry.CreateRegistries;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public class CreateRegistriesImpl {
	/** Called once from {@code Create#onCtor}; replaces NeoForge's {@code DataPackRegistryEvent.NewRegistry}. */
	public static void registerDatapackRegistries() {
		DynamicRegistries.registerSynced(
			CreateRegistries.POTATO_PROJECTILE_TYPE,
			PotatoCannonProjectileType.CODEC,
			PotatoCannonProjectileType.CODEC
		);
	}
}
