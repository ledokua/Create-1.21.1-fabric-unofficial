package com.simibubi.create.foundation.transfer.item;

import net.minecraft.world.item.ItemStack;

/** {@link IItemHandler} that additionally allows direct slot writes (bypasses insert/extract validation). */
public interface IItemHandlerModifiable extends IItemHandler {

	void setStackInSlot(int slot, ItemStack stack);
}
