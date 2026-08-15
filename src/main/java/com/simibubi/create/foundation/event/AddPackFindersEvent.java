package com.simibubi.create.foundation.event;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;

/**
 * Lifecycle event: add resource/data pack sources. On Fabric built-in packs are declared through
 * ResourceManagerHelper.registerBuiltinResourcePack — see CreateEventHooks for how upstream's handler maps.
 */
public class AddPackFindersEvent extends Event {

	private final PackType packType;

	public AddPackFindersEvent(PackType packType) {
		this.packType = packType;
	}

	public PackType getPackType() {
		return packType;
	}

	public void addRepositorySource(RepositorySource source) {
		CreatePackSources.add(packType, source);
	}
}
