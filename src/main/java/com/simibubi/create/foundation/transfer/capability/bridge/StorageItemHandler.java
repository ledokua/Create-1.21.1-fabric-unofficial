package com.simibubi.create.foundation.transfer.capability.bridge;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.transfer.item.IItemHandler;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

/**
 * {@link IItemHandler} view of a Fabric {@code Storage<ItemVariant>} (vanilla containers, other mods'
 * inventories). Slot-accurate when the storage is {@link SlottedStorage} (the common case); otherwise slots
 * are the storage's views in iteration order and inserts go to the storage as a whole.
 */
public class StorageItemHandler implements IItemHandler {

	public static IItemHandler of(Storage<ItemVariant> storage) {
		if (storage instanceof ItemHandlerStorage wrapped)
			return wrapped.handler(); // don't double-wrap our own handlers
		return new StorageItemHandler(storage);
	}

	protected final Storage<ItemVariant> storage;

	protected StorageItemHandler(Storage<ItemVariant> storage) {
		this.storage = storage;
	}

	public Storage<ItemVariant> storage() {
		return storage;
	}

	@Override
	public int getSlots() {
		if (storage instanceof SlottedStorage<ItemVariant> slotted)
			return slotted.getSlotCount();
		int i = 0;
		for (StorageView<ItemVariant> ignored : storage)
			i++;
		return i;
	}

	private StorageView<ItemVariant> view(int slot) {
		if (storage instanceof SlottedStorage<ItemVariant> slotted)
			return slot >= 0 && slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
		int i = 0;
		for (StorageView<ItemVariant> view : storage) {
			if (i++ == slot)
				return view;
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		StorageView<ItemVariant> view = view(slot);
		if (view == null || view.isResourceBlank())
			return ItemStack.EMPTY;
		return view.getResource().toStack((int) Math.min(Integer.MAX_VALUE, view.getAmount()));
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		StorageView<ItemVariant> view = view(slot);
		if (view == null)
			return stack;
		ItemVariant variant = ItemVariant.of(stack);
		try (Transaction tx = Transactions.open()) {
			long inserted;
			if (view instanceof SingleSlotStorage<ItemVariant> single)
				inserted = single.insert(variant, stack.getCount(), tx);
			else
				inserted = storage.insert(variant, stack.getCount(), tx);
			if (!simulate)
				tx.commit();
			if (inserted <= 0)
				return stack;
			return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - (int) inserted);
		}
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount <= 0)
			return ItemStack.EMPTY;
		StorageView<ItemVariant> view = view(slot);
		if (view == null || view.isResourceBlank())
			return ItemStack.EMPTY;
		ItemVariant variant = view.getResource();
		int max = Math.min(amount, variant.toStack().getMaxStackSize());
		try (Transaction tx = Transactions.open()) {
			long extracted = view.extract(variant, max, tx);
			if (!simulate)
				tx.commit();
			return extracted <= 0 ? ItemStack.EMPTY : variant.toStack((int) extracted);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		StorageView<ItemVariant> view = view(slot);
		if (view == null)
			return 0;
		return (int) Math.min(64, view.getCapacity());
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		if (stack.isEmpty())
			return false;
		StorageView<ItemVariant> view = view(slot);
		if (view == null)
			return false;
		if (!(view instanceof SingleSlotStorage<ItemVariant> single))
			return true;
		try (Transaction tx = Transactions.open()) {
			return single.insert(ItemVariant.of(stack), 1, tx) > 0;
		}
	}

	/** Snapshot of all views, for callers that want a stable list within one operation. */
	protected List<StorageView<ItemVariant>> views() {
		List<StorageView<ItemVariant>> list = new ArrayList<>();
		for (StorageView<ItemVariant> view : storage)
			list.add(view);
		return list;
	}
}
