package com.simibubi.create.foundation.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/** Which logical side the current thread is running: server thread(s) → SERVER, else CLIENT (on a client). */
public final class EffectiveSide {

	private EffectiveSide() {}

	public static LogicalSide get() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
			return LogicalSide.SERVER;
		String name = Thread.currentThread().getName();
		return name.startsWith("Server thread") || name.startsWith("Netty Server") ? LogicalSide.SERVER : LogicalSide.CLIENT;
	}
}
