package com.simibubi.create.foundation.client.extensions;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Client-side item hooks (Create-owned; was NeoForge's). Registered via {@code Item#initializeClient}. */
public interface IClientItemExtensions {

	IClientItemExtensions DEFAULT = new IClientItemExtensions() {};

	static IClientItemExtensions of(ItemStack stack) {
		return of(stack.getItem());
	}

	static IClientItemExtensions of(Item item) {
		return ClientExtensionsManager.ITEM_EXTENSIONS.getOrDefault(item, DEFAULT);
	}

	@Nullable
	default Font getFont(ItemStack stack, FontContext context) {
		return null;
	}

	@Nullable
	default HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
		return null;
	}

	default boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
		return false;
	}

	default HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
		return original;
	}

	default Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
		return getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
	}

	default void renderHelmetOverlay(ItemStack stack, Player player, int width, int height, float partialTick) {}

	default void renderHelmetOverlay(ItemStack stack, Player player, GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		renderHelmetOverlay(stack, player, guiGraphics.guiWidth(), guiGraphics.guiHeight(), deltaTracker.getGameTimeDeltaPartialTick(true));
	}

	/** Vanilla keeps its BEWLR private (NeoForge widens it); null here means "no custom renderer". Phase 3 wires non-null ones into Fabric's BuiltinItemRendererRegistry. */
	@Nullable
	default BlockEntityWithoutLevelRenderer getCustomRenderer() {
		return null;
	}

	enum FontContext {
		ITEM_COUNT, TOOLTIP, SELECTED_ITEM_NAME
	}
}
