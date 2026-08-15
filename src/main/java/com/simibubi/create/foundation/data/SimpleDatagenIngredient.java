package com.simibubi.create.foundation.data;

import com.simibubi.create.api.data.recipe.DatagenMod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Upstream: a datagen-only ingredient that serializes as a (possibly not-loaded) other mod's item. In the
 * port datagen isn't run (Phase 11), so this only has to behave at runtime: resolve the item if that mod is
 * present, else an empty ingredient.
 */
public class SimpleDatagenIngredient {

	private final DatagenMod mod;
	private final String id;

	public SimpleDatagenIngredient(DatagenMod mod, String id) {
		this.mod = mod;
		this.id = id;
	}

	public Ingredient toVanilla() {
		Item item = BuiltInRegistries.ITEM.get(mod.asResource(id));
		return item == Items.AIR ? Ingredient.EMPTY : Ingredient.of(item);
	}
}
