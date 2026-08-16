package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.ItemExtension;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

/** NeoForge's {@code isBookEnchantable} (cardboard sword only accepts knockback books). */
@Mixin(AnvilMenu.class)
public class AnvilBookEnchantableMixin {
	@ModifyExpressionValue(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z", ordinal = 0))
	private boolean create$isBookEnchantable(boolean hasStoredEnchantments, @Local(ordinal = 0) ItemStack input, @Local(ordinal = 2) ItemStack book) {
		if (!hasStoredEnchantments)
			return false;
		if (ExtensionOverrides.overrides(input.getItem(), ItemExtension.class, "isBookEnchantable", ItemStack.class, ItemStack.class))
			return input.getItem().isBookEnchantable(input, book);
		return true;
	}
}
