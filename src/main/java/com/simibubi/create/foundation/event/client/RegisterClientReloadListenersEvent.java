package com.simibubi.create.foundation.event.client;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.event.bus.Event;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

/** Lifecycle event: client resource reload listeners. Backed by Fabric's ResourceManagerHelper. */
public class RegisterClientReloadListenersEvent extends Event {

	private int counter;

	public void registerReloadListener(PreparableReloadListener listener) {
		ResourceLocation id = Create.asResource("client_reload_" + counter++ + "_" + listener.getClass().getSimpleName().toLowerCase());
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return id;
			}

			@Override
			public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller preparationsProfiler,
				ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
				return listener.reload(barrier, manager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
			}

			@Override
			public Collection<ResourceLocation> getFabricDependencies() {
				return IdentifiableResourceReloadListener.super.getFabricDependencies();
			}
		});
	}
}
