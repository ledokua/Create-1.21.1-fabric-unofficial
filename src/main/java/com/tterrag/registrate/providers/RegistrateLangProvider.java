package com.tterrag.registrate.providers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Shell — see {@link RegistrateBlockstateProvider}. */
public class RegistrateLangProvider {

	public void add(String key, String value) {}

	public void add(Item key, String name) {}

	public void add(Block key, String name) {}

	public void addBlock(java.util.function.Supplier<? extends Block> key, String name) {}

	public void addItem(java.util.function.Supplier<? extends Item> key, String name) {}

	public String getAutomaticName(java.util.function.Supplier<? extends Item> sup) {
		return toEnglishName(BuiltInRegistries.ITEM.getKey(sup.get()).getPath());
	}

	public static String toEnglishName(String internalName) {
		StringBuilder sb = new StringBuilder();
		for (String part : internalName.split("_")) {
			if (part.isEmpty())
				continue;
			sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
		}
		return sb.toString().trim();
	}
}
