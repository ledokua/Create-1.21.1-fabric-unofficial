package com.simibubi.create.foundation.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public final class FMLLoader {

	private FMLLoader() {}

	/** Physical side; upstream's {@code Dist} was already rewritten to {@link EnvType}. */
	public static EnvType getDist() {
		return FabricLoader.getInstance().getEnvironmentType();
	}

	public static boolean isProduction() {
		return !FabricLoader.getInstance().isDevelopmentEnvironment();
	}
}
