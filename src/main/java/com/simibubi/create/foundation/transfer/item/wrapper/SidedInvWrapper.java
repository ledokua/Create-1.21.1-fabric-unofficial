package com.simibubi.create.foundation.transfer.item.wrapper;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.transfer.item.IItemHandlerModifiable;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;

/** Adapts a vanilla {@link WorldlyContainer} (side-aware, e.g. furnaces) to {@link IItemHandlerModifiable}. */
public class SidedInvWrapper implements IItemHandlerModifiable {

	protected final WorldlyContainer inv;
	@Nullable
	protected final Direction side;

	public SidedInvWrapper(WorldlyContainer inv, @Nullable Direction side) {
		this.inv = inv;
		this.side = side;
	}

	public static int getSlot(WorldlyContainer inv, int slot, @Nullable Direction side) {
		int[] slots = inv.getSlotsForFace(side);
		if (slot < slots.length)
			return slots[slot];
		return -1;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || (o instanceof SidedInvWrapper other && inv.equals(other.inv) && side == other.side);
	}

	@Override
	public int hashCode() {
		return 31 * inv.hashCode() + (side == null ? 0 : side.hashCode());
	}

	@Override
	public int getSlots() {
		return inv.getSlotsForFace(side).length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		int i = getSlot(inv, slot, side);
		return i == -1 ? ItemStack.EMPTY : inv.getItem(i);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		int i = getSlot(inv, slot, side);
		if (i == -1)
			return stack;
		ItemStack existing = inv.getItem(i);
		int limit = Math.min(inv.getMaxStackSize(stack), stack.getMaxStackSize());
		if (!existing.isEmpty()) {
			if (!ItemStack.isSameItemSameComponents(stack, existing))
				return stack;
			limit -= existing.getCount();
		}
		if (limit <= 0)
			return stack;
		if (!inv.canPlaceItem(i, stack) || !inv.canPlaceItemThroughFace(i, stack, side))
			return stack;

		boolean reachedLimit = stack.getCount() > limit;
		if (!simulate) {
			if (existing.isEmpty()) {
				inv.setItem(i, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
			}
			inv.setChanged();
		}
		return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		int i = getSlot(inv, slot, side);
		if (i != -1)
			inv.setItem(i, stack);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;
		int i = getSlot(inv, slot, side);
		if (i == -1)
			return ItemStack.EMPTY;
		ItemStack existing = inv.getItem(i);
		if (existing.isEmpty())
			return ItemStack.EMPTY;
		if (!inv.canTakeItemThroughFace(i, existing, side))
			return ItemStack.EMPTY;
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (simulate)
			return existing.copyWithCount(Math.min(existing.getCount(), toExtract));
		ItemStack extracted = inv.removeItem(i, toExtract);
		inv.setChanged();
		return extracted;
	}

	@Override
	public int getSlotLimit(int slot) {
		return inv.getMaxStackSize();
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		int i = getSlot(inv, slot, side);
		return i != -1 && inv.canPlaceItem(i, stack) && inv.canPlaceItemThroughFace(i, stack, side);
	}
}
