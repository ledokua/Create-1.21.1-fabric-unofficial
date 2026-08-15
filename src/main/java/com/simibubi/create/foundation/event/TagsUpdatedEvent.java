package com.simibubi.create.foundation.event;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.core.RegistryAccess;

/** Fired after tags are (re)loaded on either side; from Fabric's CommonLifecycleEvents.TAGS_LOADED. */
public class TagsUpdatedEvent extends Event {

	private final RegistryAccess registryAccess;
	private final UpdateCause updateCause;
	private final boolean integratedServer;

	public TagsUpdatedEvent(RegistryAccess registryAccess, UpdateCause updateCause, boolean integratedServer) {
		this.registryAccess = registryAccess;
		this.updateCause = updateCause;
		this.integratedServer = integratedServer;
	}

	public RegistryAccess getRegistryAccess() {
		return registryAccess;
	}

	public UpdateCause getUpdateCause() {
		return updateCause;
	}

	/** True when this is the client's copy of tags on an integrated server (i.e. the server side already got them). */
	public boolean shouldUpdateStaticData() {
		return updateCause == UpdateCause.SERVER_DATA_LOAD || !integratedServer;
	}

	public enum UpdateCause {
		SERVER_DATA_LOAD, CLIENT_PACKET_RECEIVED
	}
}
