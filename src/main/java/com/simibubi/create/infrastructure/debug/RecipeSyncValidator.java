package com.simibubi.create.infrastructure.debug;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.Create;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Recipe sync ({@code clientbound/minecraft:update_recipes}) writes every ingredient as a list of ItemStacks, and
 * vanilla's {@code ItemStack.STREAM_CODEC} throws {@code EncoderException: Empty ItemStack not allowed} on an empty
 * one. A single recipe with an empty stack in an ingredient therefore disconnects <em>every</em> joining player, and
 * the packet error names neither the recipe nor the mod that added it. Scan the recipes once per (re)load and name
 * the culprits so the broken recipe can be removed from the pack.
 */
public class RecipeSyncValidator {

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(RecipeSyncValidator::validate);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
			if (success)
				validate(server);
		});
	}

	private static void validate(MinecraftServer server) {
		List<String> broken = new ArrayList<>();

		for (RecipeHolder<?> holder : server.getRecipeManager()
			.getRecipes()) {
			List<Ingredient> ingredients;
			try {
				ingredients = holder.value()
					.getIngredients();
			} catch (Throwable t) {
				continue; // a recipe that cannot even list its ingredients is not what breaks the packet
			}

			for (int i = 0; i < ingredients.size(); i++) {
				Ingredient ingredient = ingredients.get(i);
				ItemStack[] items;
				try {
					items = ingredient.getItems();
				} catch (Throwable t) {
					broken.add(describe(holder, i, ingredient) + " threw " + t);
					continue;
				}
				for (ItemStack stack : items) {
					if (!stack.isEmpty())
						continue;
					broken.add(describe(holder, i, ingredient) + " contains an empty ItemStack");
					break;
				}
			}
		}

		if (broken.isEmpty())
			return;

		Create.LOGGER.error(
			"{} recipe(s) contain an empty ItemStack in an ingredient. Recipe sync cannot encode those, so every player "
				+ "joining is kicked with \"Failed to encode packet 'clientbound/minecraft:update_recipes'\". This is a "
				+ "broken recipe, not a Create bug — remove or fix the recipes listed below:",
			broken.size());
		broken.forEach(line -> Create.LOGGER.error("  {}", line));
	}

	private static String describe(RecipeHolder<?> holder, int index, Ingredient ingredient) {
		CustomIngredient custom = ingredient.getCustomIngredient();
		String type = BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value()
			.getSerializer()) + (custom == null ? ""
				: ", ingredient type " + custom.getSerializer()
					.getIdentifier());
		return "recipe " + holder.id() + " (" + type + ") ingredient #" + index;
	}
}
