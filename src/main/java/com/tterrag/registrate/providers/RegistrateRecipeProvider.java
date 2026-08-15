package com.tterrag.registrate.providers;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.tterrag.registrate.util.DataIngredient;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;

/**
 * Shell — see {@link RegistrateBlockstateProvider}. Extends vanilla's RecipeProvider so all its static
 * helpers ({@code has(...)}, builders) resolve; the RecipeOutput side swallows everything.
 */
public class RegistrateRecipeProvider extends RecipeProvider implements RecipeOutput {

	private final String modid;

	public RegistrateRecipeProvider(String modid, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
		this.modid = modid;
	}

	@Override
	public void buildRecipes(RecipeOutput output) {}

	@Override
	public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {}

	@Override
	public Advancement.Builder advancement() {
		return Advancement.Builder.recipeAdvancement();
	}

	public ResourceLocation safeId(ResourceLocation id) {
		return ResourceLocation.fromNamespaceAndPath(modid, safeName(id));
	}

	public ResourceLocation safeId(DataIngredient source) {
		return safeId(source.getId());
	}

	public ResourceLocation safeId(ItemLike registryEntry) {
		return safeId(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
	}

	public String safeName(ResourceLocation id) {
		return id.getPath().replace('/', '_');
	}

	public String safeName(DataIngredient source) {
		return safeName(source.getId());
	}

	public String safeName(ItemLike registryEntry) {
		return safeName(BuiltInRegistries.ITEM.getKey(registryEntry.asItem()));
	}

	public static final int DEFAULT_SMELT_TIME = 200;
	public static final int DEFAULT_BLAST_TIME = DEFAULT_SMELT_TIME / 2;
	public static final int DEFAULT_SMOKE_TIME = DEFAULT_BLAST_TIME;
	public static final int DEFAULT_CAMPFIRE_TIME = DEFAULT_SMELT_TIME * 3;

	public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {}

	public <T extends ItemLike> void stonecutting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int resultAmount) {}

	public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {}

	public <T extends ItemLike> void smelting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {}

	public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {}

	public <T extends ItemLike> void blasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {}

	public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {}

	public <T extends ItemLike> void smoking(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {}

	public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience) {}

	public <T extends ItemLike> void campfire(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float experience, int cookingTime) {}

	public <T extends ItemLike> void smeltingAndBlasting(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float xp) {}

	public <T extends ItemLike> void food(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, float xp) {}

	public <T extends ItemLike> void square(DataIngredient source, RecipeCategory category, Supplier<? extends T> output, boolean small) {}

	public <T extends ItemLike> void storage(DataIngredient source, RecipeCategory category, Supplier<? extends T> output) {}

	public <T extends ItemLike> void storage(DataIngredient sourceUnpacked, RecipeCategory category, DataIngredient sourcePacked, Supplier<? extends T> outputPacked, Supplier<? extends T> outputUnpacked) {}

	public <T extends ItemLike> void planks(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {}

	public <T extends ItemLike> void stairs(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {}

	public <T extends ItemLike> void slab(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group, boolean stone) {}

	public <T extends ItemLike> void fence(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {}

	public <T extends ItemLike> void fenceGate(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {}

	public <T extends ItemLike> void wall(DataIngredient source, RecipeCategory category, Supplier<? extends T> result) {}

	public <T extends ItemLike> void door(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {}

	public <T extends ItemLike> void trapDoor(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, @Nullable String group) {}

	public <T extends ItemLike> void singleItem(DataIngredient source, RecipeCategory category, Supplier<? extends T> result, int required, int amount) {}

}
