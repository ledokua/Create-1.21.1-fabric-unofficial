package com.simibubi.create.impl.registry;

import com.simibubi.create.api.registry.CreateDataMaps;
import com.simibubi.create.foundation.datamap.DataMaps;
import com.simibubi.create.foundation.datamap.NeoForgeDataMaps;

/** Fabric port: registers Create's data map types with the Create-owned loader (upstream: RegisterDataMapTypesEvent). */
public class CreateDataMapsImpl {

	public static void registerDataMaps() {
		NeoForgeDataMaps.init();
		DataMaps.register(CreateDataMaps.REGULAR_BLAZE_BURNER_FUELS);
		DataMaps.register(CreateDataMaps.SUPERHEATED_BLAZE_BURNER_FUELS);
	}
}
