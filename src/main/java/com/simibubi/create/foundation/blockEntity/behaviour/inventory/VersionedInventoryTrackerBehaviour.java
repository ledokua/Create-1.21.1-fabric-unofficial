package com.simibubi.create.foundation.blockEntity.behaviour.inventory;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.transfer.capability.bridge.StorageItemHandler;
import com.simibubi.create.foundation.transfer.item.IItemHandler;

public class VersionedInventoryTrackerBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<VersionedInventoryTrackerBehaviour> TYPE = new BehaviourType<>();

	/**
	 * Fabric port: other mods' inventories have no version to wait for, so upstream retries them every tick. For big
	 * ones (storage networks, walls of chests) every retry walks thousands of slots, so with {@link #backOffOnLargeStorages()}
	 * a failed attempt there waits 2, 4, 8, 16, then 20 ticks before the next one (reset when the item changes).
	 */
	private static final int LARGE_STORAGE_SLOTS = 128;
	private static final int MAX_BACKOFF_TICKS = 20;

	private int ignoredId;
	private int ignoredVersion;
	private boolean backOffOnLargeStorages;
	private long waitUntil;
	private long lastFailure;
	private int backoff;

	public VersionedInventoryTrackerBehaviour(SmartBlockEntity be) {
		super(be);
		reset();
	}

	public boolean stillWaiting(InvManipulationBehaviour behaviour) {
		return behaviour.hasInventory() && stillWaiting(behaviour.getInventory());
	}

	public VersionedInventoryTrackerBehaviour backOffOnLargeStorages() {
		backOffOnLargeStorages = true;
		return this;
	}

	public boolean stillWaiting(IItemHandler handler) {
		if (handler instanceof VersionedInventoryWrapper viw)
			return viw.getId() == ignoredId && viw.getVersion() == ignoredVersion;
		// slot count was checked when the wait started; counting again here would walk the storage every tick
		return waitUntil != 0 && handler instanceof StorageItemHandler && gameTime() < waitUntil;
	}

	public void awaitNewVersion(InvManipulationBehaviour behaviour) {
		if (behaviour.hasInventory())
			awaitNewVersion(behaviour.getInventory());
	}

	public void awaitNewVersion(IItemHandler handler) {
		if (handler instanceof VersionedInventoryWrapper viw) {
			ignoredId = viw.getId();
			ignoredVersion = viw.getVersion();
			return;
		}
		if (!isLargeStorage(handler))
			return;
		long now = gameTime();
		boolean consecutive = backoff > 0 && now - lastFailure <= backoff + 2;
		backoff = consecutive ? Math.min(backoff * 2, MAX_BACKOFF_TICKS) : 2;
		lastFailure = now;
		waitUntil = now + backoff;
	}

	public void reset() {
		ignoredVersion = -1;
		ignoredId = -1;
		waitUntil = 0;
		backoff = 0;
	}

	private boolean isLargeStorage(IItemHandler handler) {
		return backOffOnLargeStorages && handler instanceof StorageItemHandler && handler.getSlots() >= LARGE_STORAGE_SLOTS;
	}

	private long gameTime() {
		return blockEntity.getLevel() == null ? 0 : blockEntity.getLevel().getGameTime();
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
