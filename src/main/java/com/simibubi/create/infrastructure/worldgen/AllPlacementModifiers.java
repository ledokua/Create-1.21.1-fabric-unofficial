package com.simibubi.create.infrastructure.worldgen;

import com.simibubi.create.Create;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import com.simibubi.create.foundation.event.bus.IEventBus;
import com.simibubi.create.foundation.registry.DeferredHolder;
import com.simibubi.create.foundation.registry.DeferredRegister;

import org.jetbrains.annotations.ApiStatus.Internal;

public class AllPlacementModifiers {
	private static final DeferredRegister<PlacementModifierType<?>> REGISTER = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Create.ID);

	public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<ConfigPlacementFilter>> CONFIG_FILTER = REGISTER.register("config_filter", () -> () -> ConfigPlacementFilter.CODEC);

	@Internal
	public static void register(IEventBus modEventBus) {
		REGISTER.register(modEventBus);
	}
}
