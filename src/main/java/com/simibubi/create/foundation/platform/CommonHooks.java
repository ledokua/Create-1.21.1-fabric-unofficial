package com.simibubi.create.foundation.platform;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.player.AttackEntityEvent;
import com.simibubi.create.foundation.event.entity.player.PlayerInteractEvent;
import com.simibubi.create.foundation.event.level.BlockEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Static event-firing helpers upstream takes from NeoForge; these post the bridge events. */
public final class CommonHooks {

	private CommonHooks() {}

	private static final ThreadLocal<Player> CRAFTING_PLAYER = new ThreadLocal<>();

	public static void setCraftingPlayer(@Nullable Player player) {
		CRAFTING_PLAYER.set(player);
	}

	@Nullable
	public static Player getCraftingPlayer() {
		return CRAFTING_PLAYER.get();
	}

	/** @return the cancellation result if a handler cancelled the interaction, else null */
	@Nullable
	public static InteractionResult onInteractEntity(Player player, Entity entity, InteractionHand hand) {
		PlayerInteractEvent.EntityInteract event = new PlayerInteractEvent.EntityInteract(player, hand, entity);
		CreateEventBus.BUS.post(event);
		return event.isCanceled() ? event.getCancellationResult() : null;
	}

	public static PlayerInteractEvent.LeftClickBlock onLeftClickBlock(Player player, BlockPos pos, Direction face, ServerboundPlayerActionPacket.Action action) {
		PlayerInteractEvent.LeftClickBlock.Action a = switch (action) {
			case START_DESTROY_BLOCK -> PlayerInteractEvent.LeftClickBlock.Action.START;
			case ABORT_DESTROY_BLOCK -> PlayerInteractEvent.LeftClickBlock.Action.ABORT;
			case STOP_DESTROY_BLOCK -> PlayerInteractEvent.LeftClickBlock.Action.STOP;
			default -> PlayerInteractEvent.LeftClickBlock.Action.CLIENT_HOLD;
		};
		return CreateEventBus.BUS.post(new PlayerInteractEvent.LeftClickBlock(player, pos, face, a));
	}

	public static PlayerInteractEvent.RightClickBlock onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
		return CreateEventBus.BUS.post(new PlayerInteractEvent.RightClickBlock(player, hand, pos, hitVec));
	}

	public static BlockEvent.BreakEvent fireBlockBreak(Level level, GameType gameType, net.minecraft.server.level.ServerPlayer player, BlockPos pos, BlockState state) {
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
		if (gameType.isBlockPlacingRestricted() || player.blockActionRestricted(level, pos, gameType))
			event.setCanceled(true);
		return CreateEventBus.BUS.post(event);
	}

	/** @return false if a handler cancelled the attack */
	public static boolean onPlayerAttackTarget(Player player, Entity target) {
		return !CreateEventBus.BUS.post(new AttackEntityEvent(player, target)).isCanceled();
	}
}
