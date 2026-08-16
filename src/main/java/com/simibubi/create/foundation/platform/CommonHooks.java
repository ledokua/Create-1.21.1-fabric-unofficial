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

	/** Client-side registry access provider (set by CreateClient: the current client level's registries), null on servers. */
	@Nullable
	public static java.util.function.Supplier<net.minecraft.core.RegistryAccess> clientRegistryAccess;

	/**
	 * NeoForge's {@code CommonHooks#resolveLookup}: the best available registry lookup for {@code key} — the running
	 * server's, else the connected client level's, else null.
	 */
	@Nullable
	public static <T> net.minecraft.core.HolderLookup.RegistryLookup<T> resolveLookup(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends T>> key) {
		net.minecraft.server.MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		net.minecraft.core.RegistryAccess access = server != null ? server.registryAccess() : clientRegistryAccess != null ? clientRegistryAccess.get() : null;
		if (access == null)
			return null;
		@SuppressWarnings("unchecked")
		net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>> castKey = (net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<T>>) (net.minecraft.resources.ResourceKey<?>) key;
		return access.registry(castKey).<net.minecraft.core.HolderLookup.RegistryLookup<T>>map(net.minecraft.core.Registry::asLookup).orElse(null);
	}

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
