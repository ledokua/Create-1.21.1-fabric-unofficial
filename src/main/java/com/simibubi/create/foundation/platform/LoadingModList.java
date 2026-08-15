package com.simibubi.create.foundation.platform;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;

/** FML's early mod list; only {@code getModFileById(id) != null} is used (as an "is loaded" check). */
public final class LoadingModList {

	private static final LoadingModList INSTANCE = new LoadingModList();

	private LoadingModList() {}

	public static LoadingModList get() {
		return INSTANCE;
	}

	@Nullable
	public Object getModFileById(String modId) {
		return FabricLoader.getInstance().getModContainer(modId).orElse(null);
	}
}
