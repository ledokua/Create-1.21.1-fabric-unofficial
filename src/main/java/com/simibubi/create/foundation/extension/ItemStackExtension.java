package com.simibubi.create.foundation.extension;

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

	/** NeoForge's {@code ItemStack#getAttributeModifiers()}: the stack's attribute modifiers component (or the item default). */
	default net.minecraft.world.item.component.ItemAttributeModifiers getAttributeModifiers() {
		ItemStack self = (ItemStack) (Object) this;
		return self.getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, self.getItem().getDefaultAttributeModifiers());
	}

	default net.minecraft.world.InteractionResult onItemUseFirst(net.minecraft.world.item.context.UseOnContext context) {
		ItemStack self = (ItemStack) (Object) this;
		return self.getItem().onItemUseFirst(self, context);
	}

	default boolean doesSneakBypassUse(net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player) {
		ItemStack self = (ItemStack) (Object) this;
		return self.getItem().doesSneakBypassUse(self, level, pos, player);
	}

	/** Furnace burn time in ticks (Fabric's FuelRegistry), 0 if not a fuel. The recipe-type parameter is ignored. */
	default int getBurnTime(@org.jetbrains.annotations.Nullable net.minecraft.world.item.crafting.RecipeType<?> recipeType) {
		Integer time = net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.get(((ItemStack) (Object) this).getItem());
		return time == null ? 0 : time;
	}
}
