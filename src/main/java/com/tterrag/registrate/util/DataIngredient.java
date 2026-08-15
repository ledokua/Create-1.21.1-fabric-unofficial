package com.tterrag.registrate.util;

import java.util.Arrays;
import java.util.function.Supplier;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** Recipe-datagen ingredient wrapper (Registrate). Functional but only reachable from datagen shells. */
public final class DataIngredient {

	private final Ingredient ingredient;
	private final ResourceLocation id;
	private final Supplier<Criterion<?>> criterion;

	private DataIngredient(Ingredient ingredient, ResourceLocation id, Supplier<Criterion<?>> criterion) {
		this.ingredient = ingredient;
		this.id = id;
		this.criterion = criterion;
	}

	public Ingredient toVanilla() {
		return ingredient;
	}

	public ResourceLocation getId() {
		return id;
	}

	public String getCritereonName() {
		return "has_" + id.getPath().replace('/', '_');
	}

	public Criterion<?> getCriterion(RecipeProvider prov) {
		return criterion.get();
	}

	public Criterion<?> getCritereon(RecipeProvider prov) {
		return criterion.get();
	}

	@SafeVarargs
	public static <T extends ItemLike> DataIngredient items(T first, T... others) {
		ItemLike[] all = new ItemLike[others.length + 1];
		all[0] = first;
		System.arraycopy(others, 0, all, 1, others.length);
		return new DataIngredient(Ingredient.of(all), BuiltInRegistries.ITEM.getKey(first.asItem()), () -> RecipeProvider.has(first));
	}

	public static <T extends ItemLike> DataIngredient items(Supplier<? extends T> first) {
		return items(first.get());
	}

	public static DataIngredient tag(TagKey<Item> tag) {
		return new DataIngredient(Ingredient.of(tag), tag.location(), () -> RecipeProvider.has(tag));
	}

	public static DataIngredient ingredient(Ingredient parent, ResourceLocation id, Supplier<Criterion<?>> criteria) {
		return new DataIngredient(parent, id, criteria);
	}

	public static DataIngredient ingredient(Ingredient parent, ItemLike required) {
		return ingredient(parent, BuiltInRegistries.ITEM.getKey(required.asItem()), () -> RecipeProvider.has(required));
	}

	@Override
	public String toString() {
		return "DataIngredient[" + id + "] " + Arrays.toString(ingredient.getItems());
	}
}
