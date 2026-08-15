package com.tterrag.registrate.util.entry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemProviderEntry<R, T extends R> extends RegistryEntry<R, T> implements ItemLike {

	protected ItemProviderEntry(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation id) {
		super(registryKey, id);
	}

	@Override
	public Item asItem() {
		return ((ItemLike) get()).asItem();
	}

	public ItemStack asStack() {
		return new ItemStack(this);
	}

	public ItemStack asStack(int count) {
		return new ItemStack(this, count);
	}

	public boolean isIn(ItemStack stack) {
		return isItem(stack.getItem());
	}

	public boolean isItem(Item item) {
		return asItem() == item;
	}
}
