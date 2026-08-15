package com.simibubi.create.foundation.event;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * Lifecycle event: add server-data reload listeners. Backed by Fabric's ResourceManagerHelper for
 * SERVER_DATA — the listeners collected here are registered by CreateEventHooks.
 */
public class AddReloadListenerEvent extends Event {

	private final List<PreparableReloadListener> listeners = new ArrayList<>();
	private final ReloadableServerResources serverResources;
	private final RegistryAccess registryAccess;

	public AddReloadListenerEvent(ReloadableServerResources serverResources, RegistryAccess registryAccess) {
		this.serverResources = serverResources;
		this.registryAccess = registryAccess;
	}

	public void addListener(PreparableReloadListener listener) {
		listeners.add(listener);
	}

	public List<PreparableReloadListener> getListeners() {
		return List.copyOf(listeners);
	}

	public ReloadableServerResources getServerResources() {
		return serverResources;
	}

	public RegistryAccess getRegistryAccess() {
		return registryAccess;
	}
}
