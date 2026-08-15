package com.simibubi.create.foundation.mixin.event.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.RenderFrameEvent;
import com.simibubi.create.foundation.event.client.ViewportEvent;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public abstract class GameRendererEventsMixin {
	@Shadow
	@Final
	private Camera mainCamera;

	@Inject(method = "render", at = @At("HEAD"))
	private void create$preFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		CreateEventBus.BUS.post(new RenderFrameEvent.Pre(deltaTracker));
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void create$postFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
		CreateEventBus.BUS.post(new RenderFrameEvent.Post(deltaTracker));
	}

	/**
	 * NeoForge's ComputeCameraAngles: fired right after {@code Camera#setup} in renderLevel; the returned yaw/pitch are
	 * written back into the camera and the roll rotates the pose stack (both are what vanilla reads afterwards).
	 */
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.AFTER),
		locals = LocalCapture.CAPTURE_FAILHARD)
	private void create$onCameraSetup(DeltaTracker deltaTracker, CallbackInfo ci, float partialTick, boolean bl, Camera camera, PoseStack poseStack) {
		ViewportEvent.ComputeCameraAngles event = CreateEventBus.BUS.post(new ViewportEvent.ComputeCameraAngles((GameRenderer) (Object) this, camera,
			partialTick, camera.getYRot(), camera.getXRot(), 0));
		camera.setRotation(event.getYaw(), event.getPitch());
		if (event.getRoll() != 0)
			poseStack.mulPose(Axis.ZP.rotationDegrees(event.getRoll()));
	}
}
