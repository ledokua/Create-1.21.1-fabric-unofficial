package com.simibubi.create.compat.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllItems;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders goggles worn in a trinket slot on the wearer's head (port of upstream's GogglesCurioRenderer). */
@Environment(EnvType.CLIENT)
public class TrinketsClientCompat {

	public static void init() {
		TrinketRendererRegistry.registerRenderer(AllItems.GOGGLES.get(), new GogglesTrinketRenderer());
	}

	private static class GogglesTrinketRenderer implements TrinketRenderer {
		@Override
		public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, PoseStack poseStack,
			MultiBufferSource buffer, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress,
			float headYaw, float headPitch) {
			if (!(contextModel instanceof HumanoidModel<?> model))
				return;
			poseStack.pushPose();
			// follow the head (same maths as upstream's curio renderer)
			poseStack.translate(model.head.x / 16.0, model.head.y / 16.0, model.head.z / 16.0);
			poseStack.mulPose(Axis.ZP.rotation(model.head.zRot));
			poseStack.mulPose(Axis.YP.rotation(model.head.yRot));
			poseStack.mulPose(Axis.XP.rotation(model.head.xRot));
			poseStack.translate(0, -0.25, 0);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
			poseStack.scale(0.625f, 0.625f, 0.625f);
			if (!entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
				poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
				poseStack.translate(0, -0.25, 0);
			}
			Minecraft mc = Minecraft.getInstance();
			mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, mc.level, 0);
			poseStack.popPose();
		}
	}
}
