package com.simibubi.create.foundation.event.client;

import java.io.IOException;
import java.util.function.Consumer;

import com.simibubi.create.foundation.event.bus.Event;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

/**
 * Lifecycle event: register core shaders. Backed by Fabric's CoreShaderRegistrationCallback — the
 * ShaderInstance upstream constructs eagerly is created by Fabric on our behalf instead, so
 * {@link #registerShader} takes the instance's id/format from it and re-creates it through Fabric.
 */
public class RegisterShadersEvent extends Event {

	private final ResourceProvider resourceProvider;
	private final CoreShaderRegistrationCallback.RegistrationContext context;

	public RegisterShadersEvent(ResourceProvider resourceProvider, CoreShaderRegistrationCallback.RegistrationContext context) {
		this.resourceProvider = resourceProvider;
		this.context = context;
	}

	public ResourceProvider getResourceProvider() {
		return resourceProvider;
	}

	public void registerShader(ShaderInstance shaderInstance, Consumer<ShaderInstance> onLoaded) {
		try {
			context.register(net.minecraft.resources.ResourceLocation.parse(shaderInstance.getName()), shaderInstance.getVertexFormat(), onLoaded);
		} catch (IOException e) {
			throw new RuntimeException("Failed to register shader " + shaderInstance.getName(), e);
		}
	}
}
