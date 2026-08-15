package com.simibubi.create.foundation.mixin.extension;

import java.util.function.Consumer;

import com.simibubi.create.foundation.client.extensions.IClientItemExtensions;

/** Injected onto {@code Item}: NeoForge's client-extension registration hook Create's items override. */
public interface ItemExtension {
	default void initializeClient(Consumer<IClientItemExtensions> consumer) {}

	/** NeoForge's entity-aware food lookup; the stack's food component. */
	@org.jetbrains.annotations.Nullable
	default net.minecraft.world.food.FoodProperties getFoodProperties(net.minecraft.world.item.ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity entity) {
		return stack.get(net.minecraft.core.component.DataComponents.FOOD);
	}

	/** NeoForge's pre-block-interaction item hook; vanilla has no such phase, default PASS. */
	default net.minecraft.world.InteractionResult onItemUseFirst(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.context.UseOnContext context) {
		return net.minecraft.world.InteractionResult.PASS;
	}

	private net.minecraft.world.item.Item item() {
		return (net.minecraft.world.item.Item) this;
	}

	/** Called every tick for item entities of this item; return true to skip vanilla's item-entity tick. */
	default boolean onEntityItemUpdate(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.item.ItemEntity entity) {
		return false;
	}

	default boolean supportsEnchantment(net.minecraft.world.item.ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
		return enchantment.value().canEnchant(stack);
	}

	default int getEnchantmentLevel(net.minecraft.world.item.ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
		return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
	}

	default net.minecraft.world.item.enchantment.ItemEnchantments getAllEnchantments(net.minecraft.world.item.ItemStack stack, net.minecraft.core.HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> lookup) {
		return net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(stack);
	}

	default boolean isBookEnchantable(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemStack book) {
		return true;
	}

	default boolean shouldCauseReequipAnimation(net.minecraft.world.item.ItemStack oldStack, net.minecraft.world.item.ItemStack newStack, boolean slotChanged) {
		return !oldStack.equals(newStack);
	}

	default boolean onEntitySwing(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand) {
		return onEntitySwing(stack, entity);
	}

	default boolean onEntitySwing(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
		return false;
	}

	default int getMaxStackSize(net.minecraft.world.item.ItemStack stack) {
		return stack.getMaxStackSize();
	}

	default boolean hasCustomEntity(net.minecraft.world.item.ItemStack stack) {
		return false;
	}

	@org.jetbrains.annotations.Nullable
	default net.minecraft.world.entity.Entity createEntity(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity location, net.minecraft.world.item.ItemStack stack) {
		return null;
	}

	default boolean doesSneakBypassUse(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.LevelReader level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player) {
		return false;
	}

	default boolean canPerformAction(net.minecraft.world.item.ItemStack stack, com.simibubi.create.foundation.platform.ItemAbility itemAbility) {
		return com.simibubi.create.foundation.platform.ItemAbilities.canPerform(stack, itemAbility);
	}

	@org.jetbrains.annotations.Nullable
	default net.minecraft.resources.ResourceLocation getArmorTexture(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot, net.minecraft.world.item.ArmorMaterial.Layer layer, boolean innerModel) {
		return null;
	}

	/** BlockItem hook: unregister from Item.BY_BLOCK when a block item is replaced. */
	default void removeFromBlockToItemMap(java.util.Map<net.minecraft.world.level.block.Block, net.minecraft.world.item.Item> blockToItemMap, net.minecraft.world.item.Item item) {
		if (item() instanceof net.minecraft.world.item.BlockItem blockItem)
			blockToItemMap.remove(blockItem.getBlock());
	}

	default boolean canBeHurtBy(net.minecraft.world.item.ItemStack stack, net.minecraft.world.damagesource.DamageSource source) {
		return stack.canBeHurtBy(source);
	}

	/** Mod id that added this item (NeoForge derives it from the registry key too). */
	default String getCreatorModId(net.minecraft.world.item.ItemStack stack) {
		return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
	}
}
