package com.simibubi.create.foundation.mixin.extension;

import net.minecraft.world.item.ItemStack;

/** Injected onto {@code ItemStack}; implemented by {@code ItemStackExtensionMixin}. */
public interface ItemStackExtension {

	/**
	 * NeoForge's stack-aware {@code ItemStack#getCraftingRemainingItem()} (vanilla only has the item-level
	 * {@code Item#getCraftingRemainingItem()}). Same default behaviour: the item's remainder as a stack.
	 */
	default ItemStack getCraftingRemainingItem() {
		throw new AbstractMethodError("not implemented by mixin");
	}

	default boolean hasCraftingRemainingItem() {
		throw new AbstractMethodError("not implemented by mixin");
	}

	/** NeoForge's {@code ItemStack#getEnchantmentLevel(Holder<Enchantment>)}. */
	default int getEnchantmentLevel(net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
		return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantment, (ItemStack) (Object) this);
	}

	/** NeoForge's {@code ItemStack#getTagEnchantments()}: the stack's own (non-book) enchantments. */
	default net.minecraft.world.item.enchantment.ItemEnchantments getTagEnchantments() {
		return net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting((ItemStack) (Object) this);
	}

	default boolean supportsEnchantment(net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
		return enchantment.value().canEnchant((ItemStack) (Object) this);
	}

	default boolean isComponentsPatchEmpty() {
		return ((ItemStack) (Object) this).getComponentsPatch().isEmpty();
	}

	/** NeoForge's tool-action query; see {@link com.simibubi.create.foundation.platform.ItemAbilities}. */
	default boolean canPerformAction(com.simibubi.create.foundation.platform.ItemAbility ability) {
		return com.simibubi.create.foundation.platform.ItemAbilities.canPerform((ItemStack) (Object) this, ability);
	}
}
