package com.simibubi.create.foundation.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.event.bus.Event;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** First-person hand+item rendering; fired from an ItemInHandRenderer mixin. Cancelling skips vanilla's render. */
public class RenderHandEvent extends Event implements ICancellableEvent {

	private final InteractionHand hand;
	private final PoseStack poseStack;
	private final MultiBufferSource multiBufferSource;
	private final int packedLight;
	private final float partialTick;
	private final float interpolatedPitch;
	private final float swingProgress;
	private final float equipProgress;
	private final ItemStack stack;

	public RenderHandEvent(InteractionHand hand, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick, float interpolatedPitch, float swingProgress, float equipProgress, ItemStack stack) {
		this.hand = hand;
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
		this.packedLight = packedLight;
		this.partialTick = partialTick;
		this.interpolatedPitch = interpolatedPitch;
		this.swingProgress = swingProgress;
		this.equipProgress = equipProgress;
		this.stack = stack;
	}

	public InteractionHand getHand() {
		return hand;
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

	public float getPartialTick() {
		return partialTick;
	}

	public float getInterpolatedPitch() {
		return interpolatedPitch;
	}

	public float getSwingProgress() {
		return swingProgress;
	}

	public float getEquipProgress() {
		return equipProgress;
	}

	public ItemStack getItemStack() {
		return stack;
	}
}
