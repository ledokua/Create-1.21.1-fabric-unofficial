package com.simibubi.create.foundation.recipe;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

/** "Any of" ingredient (NeoForge's CompoundIngredient) over Fabric's built-in {@code fabric:any} custom ingredient. */
public final class CompoundIngredient {

	private static final ResourceLocation ANY = ResourceLocation.fromNamespaceAndPath("fabric", "any");

	private CompoundIngredient() {}

	public static Ingredient of(Ingredient... children) {
		return DefaultCustomIngredients.any(children);
	}

	public static boolean isCompound(Ingredient ingredient) {
		CustomIngredient custom = ingredient.getCustomIngredient();
		return custom != null && ANY.equals(custom.getSerializer().getIdentifier());
	}
}
