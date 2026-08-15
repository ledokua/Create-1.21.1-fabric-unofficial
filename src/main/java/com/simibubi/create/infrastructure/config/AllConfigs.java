package com.simibubi.create.infrastructure.config;

import com.simibubi.create.Create;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.api.stress.BlockStressValues;

import net.createmod.catnip.config.ConfigBase;

import com.simibubi.create.foundation.event.bus.SubscribeEvent;
import com.simibubi.create.foundation.platform.ModContainer;
import com.simibubi.create.foundation.event.bus.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import com.simibubi.create.foundation.event.lifecycle.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber
public class AllConfigs {

	private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

	private static CClient client;
	private static CCommon common;
	private static CServer server;

	public static CClient client() {
		return client;
	}

	public static CCommon common() {
		return common;
	}

	public static CServer server() {
		return server;
	}

	public static ConfigBase byType(ModConfig.Type type) {
		return CONFIGS.get(type);
	}

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(side, config);
		return config;
	}

	public static void register() {
		client = register(CClient::new, ModConfig.Type.CLIENT);
		common = register(CCommon::new, ModConfig.Type.COMMON);
		server = register(CServer::new, ModConfig.Type.SERVER);

		// Forge Config API Port: same ModConfigSpec/ModConfig types as NeoForge, registered by mod id instead of container.
		for (Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
			fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(Create.ID, pair.getKey(), pair.getValue().specification);

		// (Re)load notifications: bridge FCAP's events onto Create's ModConfigEvent so the @SubscribeEvent handlers below fire.
		fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents.loading(Create.ID)
			.register(config -> com.simibubi.create.foundation.event.bus.CreateEventBus.BUS.post(new ModConfigEvent.Loading(config)));
		fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents.reloading(Create.ID)
			.register(config -> com.simibubi.create.foundation.event.bus.CreateEventBus.BUS.post(new ModConfigEvent.Reloading(config)));

		CStress stress = server().kinetics.stressValues;
		BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
		BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
	}

	@SubscribeEvent
	public static void onLoad(ModConfigEvent.Loading event) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == event.getConfig()
				.getSpec())
				config.onLoad();
	}

	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading event) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == event.getConfig()
				.getSpec())
				config.onReload();
	}

}
