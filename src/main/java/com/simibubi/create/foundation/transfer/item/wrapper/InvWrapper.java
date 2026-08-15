package com.simibubi.create.foundation.transfer.item.wrapper;

import com.simibubi.create.foundation.transfer.item.IItemHandlerModifiable;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Adapts a vanilla {@link Container} to {@link IItemHandlerModifiable}. */
public class InvWrapper implements IItemHandlerModifiable {

	private final Container inv;

	public InvWrapper(Container inv) {
		this.inv = inv;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || (o instanceof InvWrapper other && inv.equals(other.inv));
	}

	@Override
	public int hashCode() {
		return inv.hashCode();
	}

	@Override
	public int getSlots() {
		return inv.getContainerSize();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getItem(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		ItemStack existing = inv.getItem(slot);
		int limit = Math.min(inv.getMaxStackSize(stack), stack.getMaxStackSize());
		if (!existing.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(stack, existing))
				return stack;
			limit -= existing.getCount();
		}
		if (limit <= 0)
			return stack;
		if (!inv.canPlaceItem(slot, stack))
			return stack;

		boolean reachedLimit = stack.getCount() > limit;
		if (!simulate) {
			if (existing.isEmpty()) {
				inv.setItem(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
			}
			inv.setChanged();
		}
		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;
		ItemStack existing = inv.getItem(slot);
		if (existing.isEmpty())
			return ItemStack.EMPTY;
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (simulate) {
			return existing.copyWithCount(Math.min(existing.getCount(), toExtract));
		}
		ItemStack extracted = inv.removeItem(slot, toExtract);
		inv.setChanged();
		return extracted;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		inv.setItem(slot, stack);
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return inv.canPlaceItem(slot, stack);
	}

	public Container getInv() {
		return inv;
	}
}
