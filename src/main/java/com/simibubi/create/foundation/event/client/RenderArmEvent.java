package com.simibubi.create.foundation.event.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.event.bus.Event;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;

/** First-person bare arm rendering; fired from a PlayerRenderer mixin. Cancelling skips vanilla's render. */
public class RenderArmEvent extends Event implements ICancellableEvent {

	private final PoseStack poseStack;
	private final MultiBufferSource multiBufferSource;
	private final int packedLight;
	private final AbstractClientPlayer player;
	private final HumanoidArm arm;

	public RenderArmEvent(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
		this.poseStack = poseStack;
		this.multiBufferSource = multiBufferSource;
		this.packedLight = packedLight;
		this.player = player;
		this.arm = arm;
	}

	public HumanoidArm getArm() {
		return arm;
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

	public AbstractClientPlayer getPlayer() {
		return player;
	}
}
