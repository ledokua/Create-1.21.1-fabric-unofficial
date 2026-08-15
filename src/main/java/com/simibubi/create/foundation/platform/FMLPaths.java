package com.simibubi.create.foundation.platform;

import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

/** FML's well-known paths, over FabricLoader. */
public enum FMLPaths {
	GAMEDIR(FabricLoader.getInstance().getGameDir()),
	MODSDIR(FabricLoader.getInstance().getGameDir().resolve("mods")),
	CONFIGDIR(FabricLoader.getInstance().getConfigDir());

	private final Path path;

	FMLPaths(Path path) {
		this.path = path;
	}

	public Path get() {
		return path;
	}
}
