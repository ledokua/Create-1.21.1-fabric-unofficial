package com.simibubi.create.impl.registry;

import com.simibubi.create.api.registry.CreateDataMaps;

import com.simibubi.create.foundation.event.bus.SubscribeEvent;
import com.simibubi.create.foundation.event.bus.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber
public class CreateDataMapsImpl {
	@SubscribeEvent
	public static void registerDataMaps(RegisterDataMapTypesEvent event) {
		event.register(CreateDataMaps.REGULAR_BLAZE_BURNER_FUELS);
		event.register(CreateDataMaps.SUPERHEATED_BLAZE_BURNER_FUELS);
	}
}
