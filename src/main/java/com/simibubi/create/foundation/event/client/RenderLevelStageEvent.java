package com.simibubi.create.foundation.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix4f;

/**
 * Level render pass hooks; fired from Fabric's WorldRenderEvents (see CreateEventHooks for the stage
 * mapping — only the stages Create uses are fired).
 */
public class RenderLevelStageEvent extends Event {

	private final Stage stage;
	private final LevelRenderer levelRenderer;
	private final PoseStack poseStack;
	private final Matrix4f modelViewMatrix;
	private final Matrix4f projectionMatrix;
	private final int renderTick;
	private final DeltaTracker partialTick;
	private final Camera camera;
	private final Frustum frustum;

	public RenderLevelStageEvent(Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
		int renderTick, DeltaTracker partialTick, Camera camera, Frustum frustum) {
		this.stage = stage;
		this.levelRenderer = levelRenderer;
		this.poseStack = poseStack;
		this.modelViewMatrix = modelViewMatrix;
		this.projectionMatrix = projectionMatrix;
		this.renderTick = renderTick;
		this.partialTick = partialTick;
		this.camera = camera;
		this.frustum = frustum;
	}

	public Stage getStage() {
		return stage;
	}

	public LevelRenderer getLevelRenderer() {
		return levelRenderer;
	}

	public PoseStack getPoseStack() {
		return poseStack;
	}

	public Matrix4f getModelViewMatrix() {
		return modelViewMatrix;
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public int getRenderTick() {
		return renderTick;
	}

	public DeltaTracker getPartialTick() {
		return partialTick;
	}

	public Camera getCamera() {
		return camera;
	}

	public Frustum getFrustum() {
		return frustum;
	}

	public static final class Stage {
		public static final Stage AFTER_SKY = register("after_sky");
		public static final Stage AFTER_SOLID_BLOCKS = register("after_solid_blocks");
		public static final Stage AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS = register("after_cutout_mipped_blocks");
		public static final Stage AFTER_CUTOUT_BLOCKS = register("after_cutout_blocks");
		public static final Stage AFTER_ENTITIES = register("after_entities");
		public static final Stage AFTER_BLOCK_ENTITIES = register("after_block_entities");
		public static final Stage AFTER_TRANSLUCENT_BLOCKS = register("after_translucent_blocks");
		public static final Stage AFTER_TRIPWIRE_BLOCKS = register("after_tripwire_blocks");
		public static final Stage AFTER_PARTICLES = register("after_particles");
		public static final Stage AFTER_WEATHER = register("after_weather");
		public static final Stage AFTER_LEVEL = register("after_level");

		private final ResourceLocation name;

		private Stage(ResourceLocation name) {
			this.name = name;
		}

		private static Stage register(String name) {
			return new Stage(ResourceLocation.fromNamespaceAndPath("neoforge", name));
		}

		@Override
		public String toString() {
			return name.toString();
		}
	}
}
