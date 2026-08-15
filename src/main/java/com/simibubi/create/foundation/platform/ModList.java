package com.simibubi.create.foundation.platform;

import java.util.Optional;
import java.util.function.BiConsumer;

import net.fabricmc.loader.api.FabricLoader;

/** FML's ModList, over FabricLoader. */
public final class ModList {

	private static final ModList INSTANCE = new ModList();

	private ModList() {}

	public static ModList get() {
		return INSTANCE;
	}

	public boolean isLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	public Optional<ModContainer> getModContainerById(String modId) {
		return FabricLoader.getInstance().getModContainer(modId).map(ModContainer::new);
	}

	public void forEachModContainer(BiConsumer<String, ModContainer> consumer) {
		for (net.fabricmc.loader.api.ModContainer mod : FabricLoader.getInstance().getAllMods())
			consumer.accept(mod.getMetadata().getId(), new ModContainer(mod));
	}
}
