package com.simibubi.create.foundation.platform;

import net.fabricmc.loader.api.metadata.ModMetadata;

/** Facade over Fabric's {@link ModMetadata} with the FML accessor names Create uses. */
public record IModInfo(ModMetadata metadata) {

	public String getModId() {
		return metadata.getId();
	}

	public String getDisplayName() {
		return metadata.getName();
	}

	public String getDescription() {
		return metadata.getDescription();
	}

	public net.fabricmc.loader.api.Version getVersion() {
		return metadata.getVersion();
	}
}
