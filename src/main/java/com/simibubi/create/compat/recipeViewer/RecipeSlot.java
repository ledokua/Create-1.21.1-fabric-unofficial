package com.simibubi.create.compat.recipeViewer;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Builder for one recipe slot (subset of JEI's RecipeSlot that Create's categories use). */
public interface RecipeSlot {
	RecipeSlot addItemStack(ItemStack stack);

	RecipeSlot addItemStacks(List<ItemStack> stacks);

	default RecipeSlot addIngredients(Ingredient ingredient) {
		return addItemStacks(Arrays.asList(ingredient.getItems()));
	}

	/** Fluid alternatives; {@code displayAmount} is what the viewer shows/uses for tooltips (mB). */
	RecipeSlot addFluids(List<FluidStack> fluids, int displayAmount);

	default RecipeSlot addFluid(FluidStack stack) {
		return addFluids(List.of(stack), stack.getAmount());
	}

	/** Slot frame texture drawn behind the ingredient at ({@code x + dx}, {@code y + dy}). */
	RecipeSlot setBackground(AllGuiTextures texture, int dx, int dy);

	RecipeSlot addTooltipCallback(SlotTooltipCallback callback);

	RecipeSlot setCustomRenderer(SlotItemRenderer renderer);
}
