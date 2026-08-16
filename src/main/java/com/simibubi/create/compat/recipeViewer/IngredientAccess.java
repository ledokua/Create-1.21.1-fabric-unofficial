package com.simibubi.create.compat.recipeViewer;

import java.util.Collection;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;

/** All items / fluids the recipe viewer knows (JEI's IngredientAccess; EMI's stack list) — for generated recipes. */
public interface IngredientAccess {
	Collection<ItemStack> allItems();

	Collection<FluidStack> allFluids();
}
