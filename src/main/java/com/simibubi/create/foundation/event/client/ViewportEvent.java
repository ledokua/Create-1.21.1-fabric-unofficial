package com.simibubi.create.foundation.event.client;

import com.simibubi.create.foundation.event.bus.Event;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.shaders.FogShape;

/** Camera/fog hooks; fired from Camera#setup and FogRenderer mixins. */
public abstract class ViewportEvent extends Event {

	private final GameRenderer renderer;
	private final Camera camera;
	private final double partialTick;

	protected ViewportEvent(GameRenderer renderer, Camera camera, double partialTick) {
		this.renderer = renderer;
		this.camera = camera;
		this.partialTick = partialTick;
	}

	public GameRenderer getRenderer() {
		return renderer;
	}

	public Camera getCamera() {
		return camera;
	}

	public double getPartialTick() {
		return partialTick;
	}

	/** Fog parameters; cancel to have vanilla use the values set here instead of its own. */
	public static class RenderFog extends ViewportEvent implements ICancellableEvent {
		private final FogRenderer.FogMode mode;
		private final net.minecraft.world.level.material.FogType type;
		private float farPlaneDistance;
		private float nearPlaneDistance;
		private FogShape fogShape;

		public RenderFog(FogRenderer.FogMode mode, net.minecraft.world.level.material.FogType type, Camera camera, float partialTick, float nearPlaneDistance, float farPlaneDistance, FogShape fogShape) {
			super(null, camera, partialTick);
			this.mode = mode;
			this.type = type;
			this.farPlaneDistance = farPlaneDistance;
			this.nearPlaneDistance = nearPlaneDistance;
			this.fogShape = fogShape;
		}

		public FogRenderer.FogMode getMode() {
			return mode;
		}

		public net.minecraft.world.level.material.FogType getType() {
			return type;
		}

		public float getFarPlaneDistance() {
			return farPlaneDistance;
		}

		public float getNearPlaneDistance() {
			return nearPlaneDistance;
		}

		public FogShape getFogShape() {
			return fogShape;
		}

		public void setFarPlaneDistance(float distance) {
			farPlaneDistance = distance;
		}

		public void setNearPlaneDistance(float distance) {
			nearPlaneDistance = distance;
		}

		public void scaleFarPlaneDistance(float factor) {
			farPlaneDistance *= factor;
		}

		public void scaleNearPlaneDistance(float factor) {
			nearPlaneDistance *= factor;
		}

		public void setFogShape(FogShape shape) {
			fogShape = shape;
		}
	}

	/** Camera angles after vanilla computed them; handlers may adjust. */
	public static class ComputeCameraAngles extends ViewportEvent {
		private float yaw;
		private float pitch;
		private float roll;

		public ComputeCameraAngles(GameRenderer renderer, Camera camera, double renderPartialTicks, float yaw, float pitch, float roll) {
			super(renderer, camera, renderPartialTicks);
			this.yaw = yaw;
			this.pitch = pitch;
			this.roll = roll;
		}

		public float getYaw() {
			return yaw;
		}

		public void setYaw(float yaw) {
			this.yaw = yaw;
		}

		public float getPitch() {
			return pitch;
		}

		public void setPitch(float pitch) {
			this.pitch = pitch;
		}

		public float getRoll() {
			return roll;
		}

		public void setRoll(float roll) {
			this.roll = roll;
		}
	}
}
