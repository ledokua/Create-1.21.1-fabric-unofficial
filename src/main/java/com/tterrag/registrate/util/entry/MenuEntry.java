package com.tterrag.registrate.util.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MenuEntry<T extends AbstractContainerMenu> extends RegistryEntry<MenuType<?>, MenuType<T>> {

	public static <T extends AbstractContainerMenu> MenuEntry<T> unresolved(ResourceLocation id) {
		return new MenuEntry<>(id);
	}

	protected MenuEntry(ResourceLocation id) {
		super(Registries.MENU, id);
	}
}
