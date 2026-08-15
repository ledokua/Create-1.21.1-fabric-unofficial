package com.simibubi.create.foundation.transfer.capability;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** {@link ItemCapability} view over a Fabric {@link ItemApiLookup}. */
record DelegatingItemCapability<T, C>(ItemApiLookup<T, C> lookup) implements ItemCapability<T, C> {

	@Override
	public T find(ItemStack itemStack, C context) {
		return lookup.find(itemStack, context);
	}

	@Override
	public void registerSelf(ItemLike... items) {
		lookup.registerSelf(items);
	}

	@Override
	public void registerForItems(ItemApiProvider<T, C> provider, ItemLike... items) {
		lookup.registerForItems(provider, items);
	}

	@Override
	public void registerFallback(ItemApiProvider<T, C> fallbackProvider) {
		lookup.registerFallback(fallbackProvider);
	}

	@Override
	public ResourceLocation getId() {
		return lookup.getId();
	}

	@Override
	public Class<T> apiClass() {
		return lookup.apiClass();
	}

	@Override
	public Class<C> contextClass() {
		return lookup.contextClass();
	}

	@Override
	public ItemApiProvider<T, C> getProvider(Item item) {
		return lookup.getProvider(item);
	}
}
