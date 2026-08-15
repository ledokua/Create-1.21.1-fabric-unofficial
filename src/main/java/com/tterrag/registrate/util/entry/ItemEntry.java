package com.tterrag.registrate.util.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemEntry<T extends Item> extends ItemProviderEntry<Item, T> {

	public static <T extends Item> ItemEntry<T> unresolved(ResourceLocation id) {
		return new ItemEntry<>(id);
	}

	protected ItemEntry(ResourceLocation id) {
		super(Registries.ITEM, id);
	}
}
