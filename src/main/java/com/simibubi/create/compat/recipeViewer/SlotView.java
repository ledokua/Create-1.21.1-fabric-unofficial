package com.simibubi.create.compat.recipeViewer;

import java.util.Optional;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;

/** The ingredient a recipe slot currently shows (viewers cycle through alternatives). */
public interface SlotView {
	SlotRole getRole();

	Optional<ItemStack> getDisplayedItem();

	Optional<FluidStack> getDisplayedFluid();
}
