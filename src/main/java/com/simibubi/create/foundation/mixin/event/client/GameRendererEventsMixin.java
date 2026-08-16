package com.simibubi.create.foundation.mixin.event.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.joml.Quaternionf;

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
	 * NeoForge's ComputeCameraAngles: fired in renderLevel after camera setup + view bobbing, right where vanilla reads
	 * {@code camera.rotation()} to build the view matrix. Yaw/pitch are written back into the camera; the roll is folded
	 * into the returned quaternion (equivalent to NeoForge's {@code poseStack.mulPose(ZP.rotationDegrees(roll))} on the
	 * projection stack: view = rotZ(roll) * conj(q)  <=>  q' = q * rotZ(-roll)).
	 */
	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;rotation()Lorg/joml/Quaternionf;", ordinal = 0))
	private Quaternionf create$onCameraSetup(Quaternionf original, DeltaTracker deltaTracker) {
		Camera camera = mainCamera;
		ViewportEvent.ComputeCameraAngles event = CreateEventBus.BUS.post(new ViewportEvent.ComputeCameraAngles((GameRenderer) (Object) this, camera,
			deltaTracker.getGameTimeDeltaPartialTick(true), camera.getYRot(), camera.getXRot(), 0));
		if (event.getYaw() != camera.getYRot() || event.getPitch() != camera.getXRot())
			camera.setRotation(event.getYaw(), event.getPitch());
		Quaternionf rotation = new Quaternionf(camera.rotation());
		if (event.getRoll() != 0)
			rotation.rotateZ((float) Math.toRadians(-event.getRoll()));
		return rotation;
	}
}
