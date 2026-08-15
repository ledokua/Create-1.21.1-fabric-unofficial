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
}
