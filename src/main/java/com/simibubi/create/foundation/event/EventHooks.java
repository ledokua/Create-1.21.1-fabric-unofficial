package com.simibubi.create.foundation.event;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.EntityTeleportEvent;
import com.simibubi.create.foundation.event.level.BlockEvent;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Static helpers upstream calls to fire events from its own code paths (NeoForge's class of the same
 * name). Only the ones Create uses.
 */
public final class EventHooks {

	private EventHooks() {}

	/** No Create handler listens to item destruction; kept for call-site parity. */
	public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {}

	/** No Create handler listens to crafting; kept for call-site parity. */
	public static void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {}

	/** Posts {@link BlockEvent.EntityPlaceEvent}; returns true if the placement was cancelled. */
	public static boolean onBlockPlace(Entity entity, BlockSnapshot blockSnapshot, Direction direction) {
		BlockState placedAgainst = blockSnapshot.getLevel().getBlockState(blockSnapshot.getPos().relative(direction.getOpposite()));
		BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(blockSnapshot.getLevel(), blockSnapshot.getPos(),
			blockSnapshot.getState(), blockSnapshot.getCurrentBlock(), placedAgainst, entity);
		return CreateEventBus.BUS.post(event).isCanceled();
	}

	public static EntityTeleportEvent.ChorusFruit onChorusFruitTeleport(LivingEntity entity, double targetX, double targetY, double targetZ) {
		return CreateEventBus.BUS.post(new EntityTeleportEvent.ChorusFruit(entity, targetX, targetY, targetZ));
	}
}
