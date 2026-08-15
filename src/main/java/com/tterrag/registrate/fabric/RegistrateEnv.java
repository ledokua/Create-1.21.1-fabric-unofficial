package com.tterrag.registrate.fabric;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Replacement for upstream's {@code RegistrateDistExecutor}. The {@code Supplier<Runnable>} indirection is
 * the same trick NeoForge's DistExecutor uses: the lambda that references client classes is only
 * evaluated (and thus only class-loaded) when we're actually on the client.
 */
public final class RegistrateEnv {

	private RegistrateEnv() {}

	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static void runOnClient(Supplier<Runnable> toRun) {
		if (isClient()) {
			toRun.get().run();
		}
	}
}
