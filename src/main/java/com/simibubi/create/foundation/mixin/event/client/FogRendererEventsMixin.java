package com.simibubi.create.foundation.mixin.event.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.ViewportEvent;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;

/** NeoForge's {@code ViewportEvent.RenderFog}: fired after vanilla set up its fog; a cancelled event overrides near/far/shape. */
@Mixin(FogRenderer.class)
public class FogRendererEventsMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private static void create$onSetupFog(Camera camera, FogRenderer.FogMode mode, float farPlane, boolean thick, float partialTick, CallbackInfo ci) {
		ViewportEvent.RenderFog event = CreateEventBus.BUS.post(new ViewportEvent.RenderFog(mode, camera.getFluidInCamera(), camera, partialTick,
			RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd(), RenderSystem.getShaderFogShape()));
		if (event.isCanceled()) {
			RenderSystem.setShaderFogStart(event.getNearPlaneDistance());
			RenderSystem.setShaderFogEnd(event.getFarPlaneDistance());
			RenderSystem.setShaderFogShape(event.getFogShape());
		}
	}
}
