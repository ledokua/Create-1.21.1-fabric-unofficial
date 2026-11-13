package com.simibubi.create.foundation.transfer.capability.bridge;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.transfer.item.IItemHandler;
import com.simibubi.create.foundation.transfer.item.IItemHandlerModifiable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
 * <p>
 * Non-slotted storages (a plain {@code CombinedStorage}, storage networks) have no indexed access, and Create walks
 * inventories slot by slot ({@code for (slot < getSlots()) getStackInSlot(slot) ...}), so each slot lookup used to
 * iterate the storage again: quadratic, ~150 ms per chute insert into a full 2700-slot network. Their views are now
 * listed once and reused until the next server tick (at most {@link #VIEW_CACHE_NANOS} on other threads) or until this
 * handler changes the storage. The views stay live, so a reused list only misses views the storage added in between.
 */
public class StorageItemHandler implements IItemHandlerModifiable {

	private static final long VIEW_CACHE_NANOS = 50_000_000L;
	private static volatile int tickGeneration;

	static {
		ServerTickEvents.START_SERVER_TICK.register(server -> tickGeneration++);
	}

	public static IItemHandler of(Storage<ItemVariant> storage) {
		if (storage instanceof ItemHandlerStorage wrapped)
			return wrapped.handler(); // don't double-wrap our own handlers
		return new StorageItemHandler(storage);
	}

	protected final Storage<ItemVariant> storage;

	private List<StorageView<ItemVariant>> cachedViews;
	private int cachedGeneration;
	private long cachedAt;

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
		return cachedViews().size();
	}

	private StorageView<ItemVariant> view(int slot) {
		if (storage instanceof SlottedStorage<ItemVariant> slotted)
			return slot >= 0 && slot < slotted.getSlotCount() ? slotted.getSlot(slot) : null;
		List<StorageView<ItemVariant>> views = cachedViews();
		return slot >= 0 && slot < views.size() ? views.get(slot) : null;
	}

	private List<StorageView<ItemVariant>> cachedViews() {
		long now = System.nanoTime();
		if (cachedViews == null || cachedGeneration != tickGeneration || now - cachedAt > VIEW_CACHE_NANOS) {
			cachedViews = views();
			cachedGeneration = tickGeneration;
			cachedAt = now;
		}
		return cachedViews;
	}

	/** Called after this handler changed the storage: views may have been added or removed. */
	private void changed() {
		cachedViews = null;
	}

	/**
	 * Inserts into the storage as a whole, letting it pick the slots (Fabric's inventories fill matching stacks
	 * first). One native call instead of Create's two passes over every slot, which matters for big storages.
	 */
	public ItemStack insertAnywhere(ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		try (Transaction tx = Transactions.open()) {
			long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
			if (!simulate && inserted > 0) {
				tx.commit();
				changed();
			}
			if (inserted <= 0)
				return stack;
			return inserted >= stack.getCount() ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - (int) inserted);
		}
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
			if (!simulate) {
				tx.commit();
				changed();
			}
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
			if (!simulate) {
				tx.commit();
				changed();
			}
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

	/**
	 * Fabric port: the slot's real capacity for its current item (or for any item if it is empty). Unlike
	 * {@link #getSlotLimit} it isn't capped at a stack, so bulk storages (drawers) report what they actually hold.
	 */
	public long getSlotCapacity(int slot) {
		StorageView<ItemVariant> view = view(slot);
		return view == null ? 0 : Math.max(0, view.getCapacity());
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

	/**
	 * Fabric storages have no "set" primitive; emulate NeoForge's {@code setStackInSlot} by clearing the slot and inserting
	 * the new stack in one transaction (filters still apply, unlike a raw container write).
	 */
	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		StorageView<ItemVariant> view = view(slot);
		if (view == null)
			return;
		try (Transaction tx = Transactions.open()) {
			if (!view.isResourceBlank())
				view.extract(view.getResource(), view.getAmount(), tx);
			if (!stack.isEmpty()) {
				ItemVariant variant = ItemVariant.of(stack);
				if (view instanceof SingleSlotStorage<ItemVariant> single)
					single.insert(variant, stack.getCount(), tx);
				else
					storage.insert(variant, stack.getCount(), tx);
			}
			tx.commit();
			changed();
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
