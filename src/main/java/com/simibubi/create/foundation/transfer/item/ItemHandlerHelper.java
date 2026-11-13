package com.simibubi.create.foundation.transfer.item;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.transfer.capability.bridge.StorageItemHandler;
import com.simibubi.create.foundation.transfer.item.wrapper.PlayerMainInvWrapper;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Common {@link IItemHandler} algorithms. Create-owned (see {@link IItemHandler}). */
public final class ItemHandlerHelper {

	private ItemHandlerHelper() {}

	/** Inserts into the first slots that accept the stack, in order. Returns the remainder. */
	public static ItemStack insertItem(@Nullable IItemHandler dest, ItemStack stack, boolean simulate) {
		if (dest == null || stack.isEmpty())
			return stack;
		for (int i = 0; i < dest.getSlots(); i++) {
			stack = dest.insertItem(i, stack, simulate);
			if (stack.isEmpty())
				return ItemStack.EMPTY;
		}
		return stack;
	}

	/**
	 * Like {@link #insertItem} but first tops up slots already holding the same item, then fills empty
	 * slots — avoids fragmenting stacks across an inventory.
	 */
	public static ItemStack insertItemStacked(@Nullable IItemHandler inventory, ItemStack stack, boolean simulate) {
		if (inventory == null || stack.isEmpty())
			return stack;
		// Fabric port: other mods' storages place the stack themselves in one call (see StorageItemHandler#insertAnywhere)
		if (inventory instanceof StorageItemHandler storageHandler)
			return storageHandler.insertAnywhere(stack, simulate);
		if (!stack.isStackable())
			return insertItem(inventory, stack, simulate);

		int slots = inventory.getSlots();
		for (int i = 0; i < slots; i++) {
			ItemStack existing = inventory.getStackInSlot(i);
			if (ItemStack.isSameItemSameComponents(existing, stack)) {
				stack = inventory.insertItem(i, stack, simulate);
				if (stack.isEmpty())
					return ItemStack.EMPTY;
			}
		}
		for (int i = 0; i < slots; i++) {
			if (inventory.getStackInSlot(i).isEmpty()) {
				stack = inventory.insertItem(i, stack, simulate);
				if (stack.isEmpty())
					return ItemStack.EMPTY;
			}
		}
		return stack;
	}

	public static void giveItemToPlayer(Player player, ItemStack stack) {
		giveItemToPlayer(player, stack, -1);
	}

	/** Puts the stack in the player's inventory (preferring {@code preferredSlot}), dropping any overflow. */
	public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
		if (stack.isEmpty())
			return;
		PlayerMainInvWrapper inv = new PlayerMainInvWrapper(player.getInventory());
		Level level = player.level();
		ItemStack remainder = stack;
		if (preferredSlot >= 0 && preferredSlot < inv.getSlots())
			remainder = inv.insertItem(preferredSlot, stack, false);
		if (!remainder.isEmpty())
			remainder = insertItemStacked(inv, remainder, false);
		if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
			level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), SoundEvents.ITEM_PICKUP,
				SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			player.containerMenu.broadcastChanges();
		}
		if (!remainder.isEmpty() && !level.isClientSide) {
			Vec3 pos = player.position();
			ItemEntity entity = new ItemEntity(level, pos.x, pos.y + 0.5, pos.z, remainder);
			entity.setPickUpDelay(40);
			entity.setDeltaMovement(entity.getDeltaMovement().multiply(0, 1, 0));
			level.addFreshEntity(entity);
		}
	}

	/** Comparator-style 0..15 fill level, same formula vanilla uses for containers. */
	public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
		if (inv == null)
			return 0;
		int nonEmpty = 0;
		float proportion = 0.0F;
		for (int j = 0; j < inv.getSlots(); j++) {
			ItemStack stack = inv.getStackInSlot(j);
			if (!stack.isEmpty()) {
				proportion += (float) stack.getCount() / Math.min(inv.getSlotLimit(j), stack.getMaxStackSize());
				nonEmpty++;
			}
		}
		proportion = proportion / inv.getSlots();
		return Mth.floor(proportion * 14.0F) + (nonEmpty > 0 ? 1 : 0);
	}
}
