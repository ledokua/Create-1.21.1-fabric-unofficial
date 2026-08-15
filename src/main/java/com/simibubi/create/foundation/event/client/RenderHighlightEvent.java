package com.simibubi.create.foundation.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.event.bus.Event;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Block/entity outline rendering; fired from Fabric's WorldRenderEvents.BLOCK_OUTLINE + an entity mixin. */
public abstract class RenderHighlightEvent extends Event implements ICancellableEvent {

	private final LevelRenderer levelRenderer;
	private final Camera camera;
	private final HitResult target;
	private final DeltaTracker deltaTracker;
	private final PoseStack poseStack;
	private final MultiBufferSource multiBufferSource;

	protected RenderHighlightEvent(LevelRenderer levelRenderer, Camera camera, HitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource multiBufferSource) {
		this.levelRenderer = levelRenderer;
		this.camera = camera;
		this.target = target;
		this.deltaTracker = deltaTracker;
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
	}

	public LevelRenderer getLevelRenderer() {
		return levelRenderer;
	}

	public Camera getCamera() {
		return camera;
	}

	public HitResult getTarget() {
		return target;
	}

	public DeltaTracker getDeltaTracker() {
		return deltaTracker;
	}

	public PoseStack getPoseStack() {
		return poseStack;
	}

	public MultiBufferSource getMultiBufferSource() {
		return multiBufferSource;
	}

	public static class Block extends RenderHighlightEvent {
		public Block(LevelRenderer levelRenderer, Camera camera, BlockHitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource multiBufferSource) {
			super(levelRenderer, camera, target, deltaTracker, poseStack, multiBufferSource);
		}

		@Override
		public BlockHitResult getTarget() {
			return (BlockHitResult) super.getTarget();
		}
	}

	public static class Entity extends RenderHighlightEvent {
		public Entity(LevelRenderer levelRenderer, Camera camera, EntityHitResult target, DeltaTracker deltaTracker, PoseStack poseStack, MultiBufferSource multiBufferSource) {
			super(levelRenderer, camera, target, deltaTracker, poseStack, multiBufferSource);
		}

		@Override
		public EntityHitResult getTarget() {
			return (EntityHitResult) super.getTarget();
		}
	}
}
