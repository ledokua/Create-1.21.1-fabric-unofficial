package com.simibubi.create.foundation.transfer.item.wrapper;

import com.simibubi.create.foundation.transfer.item.IItemHandlerModifiable;

import net.minecraft.world.item.ItemStack;

/** Exposes a contiguous slot range {@code [minSlot, maxSlotExclusive)} of another handler. */
public class RangedWrapper implements IItemHandlerModifiable {

	private final IItemHandlerModifiable compose;
	private final int minSlot;
	private final int maxSlot;

	public RangedWrapper(IItemHandlerModifiable compose, int minSlot, int maxSlotExclusive) {
		if (maxSlotExclusive <= minSlot)
			throw new IllegalArgumentException("Max slot must be greater than min slot");
		this.compose = compose;
		this.minSlot = minSlot;
		this.maxSlot = maxSlotExclusive;
	}

	@Override
	public int getSlots() {
		return maxSlot - minSlot;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return checkSlot(slot) ? compose.getStackInSlot(slot + minSlot) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return checkSlot(slot) ? compose.insertItem(slot + minSlot, stack, simulate) : stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return checkSlot(slot) ? compose.extractItem(slot + minSlot, amount, simulate) : ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		if (checkSlot(slot))
			compose.setStackInSlot(slot + minSlot, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return checkSlot(slot) ? compose.getSlotLimit(slot + minSlot) : 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return checkSlot(slot) && compose.isItemValid(slot + minSlot, stack);
	}

	private boolean checkSlot(int localSlot) {
		return localSlot + minSlot < maxSlot;
	}
}
