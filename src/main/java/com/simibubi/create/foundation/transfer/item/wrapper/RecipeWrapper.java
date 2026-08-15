package com.simibubi.create.foundation.transfer.item.wrapper;

import com.simibubi.create.foundation.transfer.item.IItemHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Presents an {@link IItemHandler} as a vanilla {@link RecipeInput} for recipe matching. */
public class RecipeWrapper implements RecipeInput {

	protected final IItemHandler inv;

	public RecipeWrapper(IItemHandler inv) {
		this.inv = inv;
	}

	@Override
	public int size() {
		return inv.getSlots();
	}

	@Override
	public ItemStack getItem(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < inv.getSlots(); i++)
			if (!inv.getStackInSlot(i).isEmpty())
				return false;
		return true;
	}
}
