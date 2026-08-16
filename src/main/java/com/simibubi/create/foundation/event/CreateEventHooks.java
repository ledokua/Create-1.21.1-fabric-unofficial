package com.simibubi.create.foundation.event;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.EntityJoinLevelEvent;
import com.simibubi.create.foundation.event.entity.EntityLeaveLevelEvent;
import com.simibubi.create.foundation.event.entity.living.LivingEquipmentChangeEvent;
import com.simibubi.create.foundation.event.entity.player.AttackEntityEvent;
import com.simibubi.create.foundation.event.entity.player.PlayerEvent;
import com.simibubi.create.foundation.event.entity.player.PlayerInteractEvent;
import com.simibubi.create.foundation.event.level.BlockEvent;
import com.simibubi.create.foundation.event.level.ChunkEvent;
import com.simibubi.create.foundation.event.level.LevelEvent;
import com.simibubi.create.foundation.event.server.ServerStoppingEvent;
import com.simibubi.create.foundation.event.tick.LevelTickEvent;
import com.simibubi.create.foundation.event.tick.ServerTickEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * Wires Fabric's callbacks to Create's NeoForge-shaped event bridge ({@code foundation.event.*}), so the
 * ~70 upstream {@code @SubscribeEvent} handlers keep working unchanged. Events Fabric has no callback for
 * are posted from small mixins in {@code foundation.mixin.event} (see PORTING_PLAN.md, "Event wiring").
 * <p>
 * Common (both sides) hooks live here; client-only ones in {@link CreateClientEventHooks}.
 */
public final class CreateEventHooks {

	private CreateEventHooks() {}

	private static <T extends com.simibubi.create.foundation.event.bus.Event> T post(T event) {
		return CreateEventBus.BUS.post(event);
	}

	/** Posted after registration (NeoForge fires AddPackFindersEvent post-registry); the pack repository mixin drains CreatePackSources. */
	public static void postPackFinders() {
		post(new AddPackFindersEvent(PackType.SERVER_DATA));
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			post(new AddPackFindersEvent(PackType.CLIENT_RESOURCES));
	}

	public static void register() {
		// ---- ticks ----
		ServerTickEvents.START_SERVER_TICK.register(server -> post(new ServerTickEvent.Pre(server::haveTime, server)));
		ServerTickEvents.END_SERVER_TICK.register(server -> post(new ServerTickEvent.Post(server::haveTime, server)));
		ServerTickEvents.START_WORLD_TICK.register(level -> post(new LevelTickEvent.Pre(level.getServer()::haveTime, level)));
		ServerTickEvents.END_WORLD_TICK.register(level -> post(new LevelTickEvent.Post(level.getServer()::haveTime, level)));

		// ---- server / level lifecycle ----
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> post(new ServerStoppingEvent(server)));
		ServerWorldEvents.LOAD.register((server, level) -> post(new LevelEvent.Load(level)));
		ServerWorldEvents.UNLOAD.register((server, level) -> post(new LevelEvent.Unload(level)));
		ServerChunkEvents.CHUNK_LOAD.register((level, chunk) -> post(new ChunkEvent.Load(chunk, level, false)));
		ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> post(new ChunkEvent.Unload(chunk, level)));
		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) ->
			post(new TagsUpdatedEvent(registries, client ? TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED : TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD, !client && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)));

		// ---- entities ----
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> post(new EntityJoinLevelEvent(entity, level)));
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> post(new EntityLeaveLevelEvent(entity, level)));
		ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, next) -> post(new LivingEquipmentChangeEvent(entity, slot, previous, next)));

		// ---- players ----
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> post(new PlayerEvent.PlayerLoggedInEvent(handler.player)));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> post(new PlayerEvent.PlayerLoggedOutEvent(handler.player)));

		// ---- interaction ----
		UseBlockCallback.EVENT.register(CreateEventHooks::onUseBlock);
		UseItemCallback.EVENT.register(CreateEventHooks::onUseItem);
		UseEntityCallback.EVENT.register(CreateEventHooks::onUseEntity);
		AttackBlockCallback.EVENT.register(CreateEventHooks::onAttackBlock);
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) ->
			post(new AttackEntityEvent(player, entity)).isCanceled() ? InteractionResult.FAIL : InteractionResult.PASS);
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
			!post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled());

		// ---- commands / data ----
		CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) ->
			post(new RegisterCommandsEvent(dispatcher, context, selection)));
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			// AddReloadListenerEvent is posted once per data reload; upstream's handler adds listeners that we then run
			// in-line here (they're all synchronous apply-only listeners in Create).
			@Override
			public ResourceLocation getFabricId() {
				return Create.asResource("reload_listeners");
			}

			@Override
			public void onResourceManagerReload(ResourceManager manager) {
				MinecraftServer server = com.simibubi.create.foundation.platform.ServerLifecycleHooks.getCurrentServer();
				if (server == null)
					return;
				AddReloadListenerEvent event = post(new AddReloadListenerEvent(server.resources.managers(), server.registryAccess()));
				for (PreparableReloadListener listener : event.getListeners()) {
					listener.reload(CompletableFutureBarrier.INSTANCE, manager, net.minecraft.util.profiling.InactiveProfiler.INSTANCE,
						net.minecraft.util.profiling.InactiveProfiler.INSTANCE, Runnable::run, Runnable::run).join();
				}
			}
		});


	}

	// ---- interaction adapters ----

	private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		PlayerInteractEvent.RightClickBlock event = post(new PlayerInteractEvent.RightClickBlock(player, hand, hitResult.getBlockPos(), hitResult));
		if (event.isCanceled())
			return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
		// NeoForge's setUseBlock/setUseItem(FALSE) can't be expressed through Fabric's callback; DENY-ing everything is the
		// closest (Create only uses them to suppress the block interaction while it handles the click itself).
		if (event.getUseBlock() == net.fabricmc.fabric.api.util.TriState.FALSE || event.getUseItem() == net.fabricmc.fabric.api.util.TriState.FALSE)
			return InteractionResult.FAIL;
		// NeoForge's Item#onItemUseFirst: runs before the block's use handler, on both sides (cogwheels, linked controller)
		net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
		if (!player.isSpectator() && com.simibubi.create.foundation.extension.ExtensionOverrides.overrides(stack.getItem(),
			com.simibubi.create.foundation.extension.ItemExtension.class, "onItemUseFirst", net.minecraft.world.item.ItemStack.class, net.minecraft.world.item.context.UseOnContext.class)) {
			InteractionResult result = stack.getItem().onItemUseFirst(stack, new net.minecraft.world.item.context.UseOnContext(player, hand, hitResult));
			if (result != InteractionResult.PASS)
				return result;
		}
		return InteractionResult.PASS;
	}

	private static InteractionResultHolder<net.minecraft.world.item.ItemStack> onUseItem(Player player, Level level, InteractionHand hand) {
		PlayerInteractEvent.RightClickItem event = post(new PlayerInteractEvent.RightClickItem(player, hand));
		if (event.isCanceled())
			return new InteractionResultHolder<>(event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult(), player.getItemInHand(hand));
		return InteractionResultHolder.pass(player.getItemInHand(hand));
	}

	private static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
		// Fabric fires once with the hit result (== NeoForge's EntityInteractSpecific) and once without (== EntityInteract).
		PlayerInteractEvent event = hitResult != null
			? post(new PlayerInteractEvent.EntityInteractSpecific(player, hand, entity, hitResult.getLocation().subtract(entity.position())))
			: post(new PlayerInteractEvent.EntityInteract(player, hand, entity));
		if (((com.simibubi.create.foundation.event.bus.ICancellableEvent) event).isCanceled())
			return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
		return InteractionResult.PASS;
	}

	private static InteractionResult onAttackBlock(Player player, Level level, InteractionHand hand, BlockPos pos, Direction direction) {
		PlayerInteractEvent.LeftClickBlock event = post(new PlayerInteractEvent.LeftClickBlock(player, pos, direction, PlayerInteractEvent.LeftClickBlock.Action.START));
		if (event.isCanceled() || event.getUseBlock() == net.fabricmc.fabric.api.util.TriState.FALSE || event.getUseItem() == net.fabricmc.fabric.api.util.TriState.FALSE)
			return event.getCancellationResult() == InteractionResult.PASS ? InteractionResult.FAIL : event.getCancellationResult();
		return InteractionResult.PASS;
	}

	/** Barrier that runs the apply phase immediately (for synchronous listeners). */
	private enum CompletableFutureBarrier implements PreparableReloadListener.PreparationBarrier {
		INSTANCE;

		@Override
		public <T> java.util.concurrent.CompletableFuture<T> wait(T backgroundResult) {
			return java.util.concurrent.CompletableFuture.completedFuture(backgroundResult);
		}
	}
}
