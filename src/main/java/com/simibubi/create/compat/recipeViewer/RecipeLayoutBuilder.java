package com.simibubi.create.compat.recipeViewer;

/** Viewer-neutral recipe layout (JEI RecipeLayoutBuilder / EMI WidgetHolder), filled by {@code CreateRecipeCategory#setRecipe}. */
public interface RecipeLayoutBuilder {
	RecipeSlot addSlot(SlotRole role, int x, int y);

	/** Ingredients that take part in the recipe (for lookups) but aren't drawn (JEI's addInvisibleIngredients). */
	RecipeSlot addInvisibleIngredients(SlotRole role);
}
