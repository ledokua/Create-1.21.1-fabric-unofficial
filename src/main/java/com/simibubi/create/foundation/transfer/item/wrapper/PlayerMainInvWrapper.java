package com.simibubi.create.foundation.transfer.item.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** The player's main inventory (hotbar + storage, not armor/offhand) as an item handler. */
public class PlayerMainInvWrapper extends RangedWrapper {

	private final Inventory inventoryPlayer;

	public PlayerMainInvWrapper(Inventory inv) {
		super(new InvWrapper(inv), 0, inv.items.size());
		inventoryPlayer = inv;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack rest = super.insertItem(slot, stack, simulate);
		if (rest.getCount() != stack.getCount()) {
			// pop-time for the pickup animation, same as vanilla's Inventory#add does
			ItemStack inSlot = getStackInSlot(slot);
			if (!inSlot.isEmpty()) {
				if (getInventoryPlayer().player.level().isClientSide) {
					inSlot.setPopTime(5);
				} else if (getInventoryPlayer().player instanceof net.minecraft.server.level.ServerPlayer sp) {
					sp.containerMenu.broadcastChanges();
				}
			}
		}
		return rest;
	}

	public Inventory getInventoryPlayer() {
		return inventoryPlayer;
	}
}
