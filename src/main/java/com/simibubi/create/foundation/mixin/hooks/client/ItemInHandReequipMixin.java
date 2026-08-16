package com.simibubi.create.foundation.mixin.hooks.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.ItemExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

/**
 * NeoForge's {@code shouldCauseReequipAnimation} (zapper/potato cannon don't re-equip on component changes).
 * Vanilla re-equips whenever the stacks stop matching; NeoForge asks the old item instead. Wrapping {@code matches}
 * (ordinal 0 = main hand, 1 = off hand) with the inverse of that keeps the rest of {@code tick()} identical.
 */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandReequipMixin {
	@Unique
	private int create$lastSelectedSlot = -1;

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 0))
	private boolean create$mainHandReequip(ItemStack from, ItemStack to, Operation<Boolean> original) {
		int slot = Minecraft.getInstance().player.getInventory().selected;
		boolean slotChanged = slot != create$lastSelectedSlot;
		create$lastSelectedSlot = slot;
		return !create$shouldCauseReequip(from, to, slotChanged, original);
	}

	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", ordinal = 1))
	private boolean create$offHandReequip(ItemStack from, ItemStack to, Operation<Boolean> original) {
		return !create$shouldCauseReequip(from, to, false, original);
	}

	private static boolean create$shouldCauseReequip(ItemStack from, ItemStack to, boolean slotChanged, Operation<Boolean> matches) {
		if (from.isEmpty() && to.isEmpty())
			return false;
		if (from.isEmpty() || to.isEmpty())
			return true;
		if (ExtensionOverrides.overrides(from.getItem(), ItemExtension.class, "shouldCauseReequipAnimation", ItemStack.class, ItemStack.class, boolean.class))
			return from.getItem().shouldCauseReequipAnimation(from, to, slotChanged);
		return !matches.call(from, to);
	}
}
