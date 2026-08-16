package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.ItemExtension;

import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Bridges NeoForge's {@code Item#supportsEnchantment(stack, enchantment)} onto Fabric's {@code FabricItem#canBeEnchantedWith}
 * (which vanilla's enchanting paths consult through Fabric API's mixins). Only kicks in for items overriding the NeoForge hook.
 */
@Mixin(Item.class)
public abstract class ItemEnchantmentHooksMixin {
	// FabricItem is interface-injected onto Item; overriding its default here.
	public boolean canBeEnchantedWith(ItemStack stack, Holder<Enchantment> enchantment, EnchantingContext context) {
		Item self = (Item) (Object) this;
		if (ExtensionOverrides.overrides(self, ItemExtension.class, "supportsEnchantment", ItemStack.class, Holder.class))
			return self.supportsEnchantment(stack, enchantment);
		// Fabric's default
		return context == EnchantingContext.PRIMARY ? enchantment.value().isPrimaryItem(stack) : enchantment.value().isSupportedItem(stack);
	}
}
