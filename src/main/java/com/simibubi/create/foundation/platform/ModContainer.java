package com.simibubi.create.foundation.platform;

/** Facade over a Fabric {@link net.fabricmc.loader.api.ModContainer}. */
public record ModContainer(net.fabricmc.loader.api.ModContainer fabric) {

	public String getModId() {
		return fabric.getMetadata().getId();
	}

	public IModInfo getModInfo() {
		return new IModInfo(fabric.getMetadata());
	}
}
