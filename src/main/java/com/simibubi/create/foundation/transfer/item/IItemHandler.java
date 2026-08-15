package com.simibubi.create.foundation.transfer.item;

import net.minecraft.world.item.ItemStack;

/**
 * Slot-based item inventory abstraction Create's logistics are written against. Create-owned port of the
 * contract upstream gets from NeoForge (see PORTING_PLAN.md, "NeoForge → Fabric translation strategy");
 * bridged to Fabric's Transfer API at lookup boundaries by {@link com.simibubi.create.foundation.transfer.capability.Capabilities}.
 * <p>
 * Contract: returned stacks are never null ({@link ItemStack#EMPTY} instead) and must not be mutated by
 * callers; {@code simulate=true} calls must have no side effects.
 */
public interface IItemHandler {

	int getSlots();

	/** The stack in the slot — a live reference for cheap inspection; do NOT modify it. */
	ItemStack getStackInSlot(int slot);

	/**
	 * Inserts into the given slot.
	 * @return the remainder that could not be inserted (EMPTY if fully inserted). If nothing fits, the
	 *         original stack (same instance) is returned.
	 */
	ItemStack insertItem(int slot, ItemStack stack, boolean simulate);

	/**
	 * Extracts up to {@code amount} from the given slot.
	 * @return what was (or would be) extracted; EMPTY if nothing.
	 */
	ItemStack extractItem(int slot, int amount, boolean simulate);

	/** Max stack size the slot accepts regardless of item (usually 64). */
	int getSlotLimit(int slot);

	boolean isItemValid(int slot, ItemStack stack);
}
