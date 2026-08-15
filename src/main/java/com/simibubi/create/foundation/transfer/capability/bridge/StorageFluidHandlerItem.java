package com.simibubi.create.foundation.transfer.capability.bridge;

import com.simibubi.create.foundation.transfer.fluid.IFluidHandlerItem;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

/** {@link StorageFluidHandler} over an item's fluid storage; {@link #getContainer()} reflects modifications. */
public class StorageFluidHandlerItem extends StorageFluidHandler implements IFluidHandlerItem {

	/** Wraps a detached stack in a context whose modifications we can read back. */
	@Nullable
	public static StorageFluidHandlerItem of(ItemStack stack) {
		StackSlot slot = new StackSlot(stack.copy());
		ContainerItemContext context = ContainerItemContext.ofSingleSlot(slot);
		Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
		return storage == null ? null : new StorageFluidHandlerItem(slot, storage);
	}

	private final StackSlot slot;

	private StorageFluidHandlerItem(StackSlot slot, Storage<FluidVariant> storage) {
		super(storage);
		this.slot = slot;
	}

	@Override
	public ItemStack getContainer() {
		return slot.getStack();
	}

	private static final class StackSlot extends SingleStackStorage {
		private ItemStack stack;

		StackSlot(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		protected ItemStack getStack() {
			return stack;
		}

		@Override
		protected void setStack(ItemStack stack) {
			this.stack = stack;
		}
	}
}
