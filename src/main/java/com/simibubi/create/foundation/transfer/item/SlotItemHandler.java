package com.simibubi.create.foundation.transfer.item;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** A menu {@link Slot} backed by an {@link IItemHandler} slot instead of a vanilla {@link Container}. */
public class SlotItemHandler extends Slot {

	private static final Container EMPTY = new SimpleContainer(0);
	protected final IItemHandler itemHandler;
	protected final int index;

	public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(EMPTY, index, xPosition, yPosition);
		this.itemHandler = itemHandler;
		this.index = index;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack.isEmpty())
			return false;
		return itemHandler.isItemValid(index, stack);
	}

	@Override
	public ItemStack getItem() {
		return itemHandler.getStackInSlot(index);
	}

	@Override
	public void set(ItemStack stack) {
		((IItemHandlerModifiable) itemHandler).setStackInSlot(index, stack);
		setChanged();
	}

	/** NeoForge-added Slot hook (initial menu sync); no vanilla counterpart, kept for call-site parity. */
	public void initialize(ItemStack stack) {
		((IItemHandlerModifiable) itemHandler).setStackInSlot(index, stack);
		setChanged();
	}

	@Override
	public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {}

	@Override
	public int getMaxStackSize() {
		return itemHandler.getSlotLimit(index);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		ItemStack maxAdd = stack.copyWithCount(stack.getMaxStackSize());
		ItemStack current = itemHandler.getStackInSlot(index);
		if (itemHandler instanceof IItemHandlerModifiable modifiable) {
			modifiable.setStackInSlot(index, ItemStack.EMPTY);
			ItemStack remainder = modifiable.insertItem(index, maxAdd, true);
			modifiable.setStackInSlot(index, current);
			return maxAdd.getCount() - remainder.getCount();
		}
		ItemStack remainder = itemHandler.insertItem(index, maxAdd, true);
		int limit = current.getCount() + maxAdd.getCount() - remainder.getCount();
		return Math.min(limit, stack.getMaxStackSize());
	}

	@Override
	public boolean mayPickup(Player playerIn) {
		return !itemHandler.extractItem(index, 1, true).isEmpty();
	}

	@Override
	public ItemStack remove(int amount) {
		return itemHandler.extractItem(index, amount, false);
	}

	public IItemHandler getItemHandler() {
		return itemHandler;
	}

	/** NeoForge-added Slot hook used by its quick-move logic; kept for call-site parity. */
	public boolean isSameInventory(Slot other) {
		return other instanceof SlotItemHandler s && s.getItemHandler() == itemHandler;
	}
}
