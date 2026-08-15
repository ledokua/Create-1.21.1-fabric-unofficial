package com.simibubi.create.foundation.transfer.fluid;

import net.minecraft.world.item.ItemStack;

/** A fluid handler backed by an item (bucket, tank item, ...). {@link #getContainer()} is the possibly-modified stack. */
public interface IFluidHandlerItem extends IFluidHandler {

	ItemStack getContainer();
}
