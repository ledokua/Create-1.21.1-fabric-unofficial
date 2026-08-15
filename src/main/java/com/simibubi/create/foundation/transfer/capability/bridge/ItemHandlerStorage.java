package com.simibubi.create.foundation.transfer.capability.bridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.foundation.transfer.item.IItemHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

/**
 * Exposes an {@link IItemHandler} (Create's inventories) to Fabric's Transfer API so hoppers and other mods
 * can use them. Handlers aren't transactional, so operations are simulated during the transaction and
 * applied when the outermost transaction commits (see {@link Transactions#applyOnCommit}). Known limitation
 * shared with every such bridge: several operations on the same handler within one transaction each
 * simulate against the pre-transaction state.
 */
public class ItemHandlerStorage implements SlottedStorage<ItemVariant> {

	private final IItemHandler handler;

	public ItemHandlerStorage(IItemHandler handler) {
		this.handler = handler;
	}

	public IItemHandler handler() {
		return handler;
	}

	@Override
	public int getSlotCount() {
		return handler.getSlots();
	}

	@Override
	public SingleSlotStorage<ItemVariant> getSlot(int slot) {
		return new Slot(slot);
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext tx) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long remaining = maxAmount;
		int slots = handler.getSlots();
		// same two-pass strategy as ItemHandlerHelper.insertItemStacked, at the slot level
		for (int pass = 0; pass < 2 && remaining > 0; pass++) {
			for (int i = 0; i < slots && remaining > 0; i++) {
				ItemStack existing = handler.getStackInSlot(i);
				boolean matchPass = pass == 0 ? !existing.isEmpty() && resource.matches(existing) : existing.isEmpty();
				if (!matchPass)
					continue;
				remaining -= new Slot(i).insert(resource, remaining, tx);
			}
		}
		return maxAmount - remaining;
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext tx) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		long remaining = maxAmount;
		int slots = handler.getSlots();
		for (int i = 0; i < slots && remaining > 0; i++)
			remaining -= new Slot(i).extract(resource, remaining, tx);
		return maxAmount - remaining;
	}

	@Override
	public Iterator<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant>> iterator() {
		List<net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant>> views = new ArrayList<>(handler.getSlots());
		for (int i = 0; i < handler.getSlots(); i++)
			views.add(new Slot(i));
		return views.iterator();
	}

	private final class Slot implements SingleSlotStorage<ItemVariant> {
		private final int index;

		Slot(int index) {
			this.index = index;
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext tx) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);
			int count = (int) Math.min(maxAmount, resource.toStack().getMaxStackSize());
			ItemStack stack = resource.toStack(count);
			ItemStack remainder = handler.insertItem(index, stack, true);
			int inserted = count - remainder.getCount();
			if (inserted <= 0)
				return 0;
			ItemStack toInsert = resource.toStack(inserted);
			Transactions.applyOnCommit(tx, () -> handler.insertItem(index, toInsert, false));
			return inserted;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext tx) {
			StoragePreconditions.notBlankNotNegative(resource, maxAmount);
			ItemStack inSlot = handler.getStackInSlot(index);
			if (inSlot.isEmpty() || !resource.matches(inSlot))
				return 0;
			int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
			ItemStack extracted = handler.extractItem(index, amount, true);
			if (extracted.isEmpty())
				return 0;
			int n = extracted.getCount();
			Transactions.applyOnCommit(tx, () -> handler.extractItem(index, n, false));
			return n;
		}

		@Override
		public boolean isResourceBlank() {
			return handler.getStackInSlot(index).isEmpty();
		}

		@Override
		public ItemVariant getResource() {
			return ItemVariant.of(handler.getStackInSlot(index));
		}

		@Override
		public long getAmount() {
			return handler.getStackInSlot(index).getCount();
		}

		@Override
		public long getCapacity() {
			ItemStack stack = handler.getStackInSlot(index);
			int limit = handler.getSlotLimit(index);
			return stack.isEmpty() ? limit : Math.min(limit, stack.getMaxStackSize());
		}
	}
}
