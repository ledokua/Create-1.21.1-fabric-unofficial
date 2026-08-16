package com.simibubi.create.foundation.mixin.hooks.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.client.extensions.IClientItemExtensions;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** NeoForge's {@code IClientItemExtensions#renderHelmetOverlay} (cardboard armor blur), next to vanilla's pumpkin blur. */
@Mixin(Gui.class)
public class HelmetOverlayMixin {
	@Inject(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getArmor(I)Lnet/minecraft/world/item/ItemStack;", shift = At.Shift.AFTER))
	private void create$renderHelmetOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;
		ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
		if (helmet.isEmpty())
			return;
		IClientItemExtensions.of(helmet).renderHelmetOverlay(helmet, mc.player, guiGraphics, deltaTracker);
	}
}
