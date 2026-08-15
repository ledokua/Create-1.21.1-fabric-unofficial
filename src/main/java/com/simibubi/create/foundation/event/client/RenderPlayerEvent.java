package com.simibubi.create.foundation.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;
import com.simibubi.create.foundation.event.entity.player.PlayerEvent;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;

/** Third-person player rendering; fired from a PlayerRenderer#render mixin. */
public abstract class RenderPlayerEvent extends PlayerEvent {

	private final PlayerRenderer renderer;
	private final float partialTick;
	private final PoseStack poseStack;
	private final MultiBufferSource multiBufferSource;
	private final int packedLight;

	protected RenderPlayerEvent(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
		super(player);
		this.renderer = renderer;
		this.partialTick = partialTick;
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
		this.packedLight = packedLight;
	}

	public PlayerRenderer getRenderer() {
		return renderer;
	}

	public float getPartialTick() {
		return partialTick;
	}

	public PoseStack getPoseStack() {
		return poseStack;
	}

	public MultiBufferSource getMultiBufferSource() {
		return multiBufferSource;
	}

	public int getPackedLight() {
		return packedLight;
	}

	/** Cancellable: skips vanilla's player render. */
	public static class Pre extends RenderPlayerEvent implements ICancellableEvent {
		public Pre(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
			super(player, renderer, partialTick, poseStack, multiBufferSource, packedLight);
		}
	}

	public static class Post extends RenderPlayerEvent {
		public Post(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
			super(player, renderer, partialTick, poseStack, multiBufferSource, packedLight);
		}
	}
}
