package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.foundation.extension.ItemStackExtension;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackExtensionMixin implements ItemStackExtension {

	@Override
	public ItemStack getCraftingRemainingItem() {
		Item remainder = ((ItemStack) (Object) this).getItem().getCraftingRemainingItem();
		return remainder == null ? ItemStack.EMPTY : new ItemStack(remainder);
	}

	@Override
	public boolean hasCraftingRemainingItem() {
		return ((ItemStack) (Object) this).getItem().hasCraftingRemainingItem();
	}
}
