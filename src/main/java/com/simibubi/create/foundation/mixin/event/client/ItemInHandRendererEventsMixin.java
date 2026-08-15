package com.simibubi.create.foundation.mixin.event.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.RenderHandEvent;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererEventsMixin {
	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void create$onRenderHand(AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress,
		ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		if (CreateEventBus.BUS.post(new RenderHandEvent(hand, poseStack, buffer, packedLight, partialTick, pitch, swingProgress, equipProgress, stack)).isCanceled())
			ci.cancel();
	}
}
