package com.simibubi.create.compat.recipeViewer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.simibubi.create.foundation.item.ItemHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

/** Client-side recipe lookups shared by the JEI and EMI plugins (was static helpers on CreateJEI). */
@Environment(EnvType.CLIENT)
public final class RecipeCollector {

	private RecipeCollector() {}

	public static void consumeAllRecipes(Consumer<? super RecipeHolder<?>> consumer) {
		Minecraft.getInstance()
			.getConnection()
			.getRecipeManager()
			.getRecipes()
			.forEach(consumer);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T extends Recipe<?>> void consumeTypedRecipes(Consumer<RecipeHolder<?>> consumer, RecipeType<?> type) {
		List<? extends RecipeHolder<?>> map = Minecraft.getInstance()
			.getConnection()
			.getRecipeManager().getAllRecipesFor((RecipeType) type);
		if (!map.isEmpty())
			map.forEach(consumer);
	}

	public static List<RecipeHolder<?>> getTypedRecipes(RecipeType<?> type) {
		List<RecipeHolder<?>> recipes = new ArrayList<>();
		consumeTypedRecipes(recipes::add, type);
		return recipes;
	}

	public static List<RecipeHolder<?>> getTypedRecipesExcluding(RecipeType<?> type, Predicate<RecipeHolder<?>> exclusionPred) {
		List<RecipeHolder<?>> recipes = getTypedRecipes(type);
		recipes.removeIf(exclusionPred);
		return recipes;
	}

	public static boolean doInputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		if (recipe1.getIngredients().isEmpty() || recipe2.getIngredients().isEmpty())
			return false;
		ItemStack[] matchingStacks = recipe1.getIngredients().getFirst().getItems();
		if (matchingStacks.length == 0)
			return false;
		return recipe2.getIngredients().getFirst().test(matchingStacks[0]);
	}

	public static boolean doOutputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
		return ItemHelper.sameItem(recipe1.getResultItem(registryAccess), recipe2.getResultItem(registryAccess));
	}
}
