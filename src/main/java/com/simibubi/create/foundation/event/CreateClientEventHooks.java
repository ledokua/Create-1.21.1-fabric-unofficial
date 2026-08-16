package com.simibubi.create.foundation.event;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.ClientPlayerNetworkEvent;
import com.simibubi.create.foundation.event.client.RegisterClientReloadListenersEvent;
import com.simibubi.create.foundation.event.client.RegisterGuiLayersEvent;
import com.simibubi.create.foundation.event.client.RegisterItemDecorationsEvent;
import com.simibubi.create.foundation.event.client.RegisterKeyMappingsEvent;
import com.simibubi.create.foundation.event.client.RegisterShadersEvent;
import com.simibubi.create.foundation.event.client.RenderHighlightEvent;
import com.simibubi.create.foundation.event.client.RenderLevelStageEvent;
import com.simibubi.create.foundation.event.client.RenderLevelStageEvent.Stage;
import com.simibubi.create.foundation.event.client.ScreenEvent;
import com.simibubi.create.foundation.event.client.map.RegisterMapDecorationRenderersEvent;
import com.simibubi.create.foundation.event.entity.EntityJoinLevelEvent;
import com.simibubi.create.foundation.event.entity.EntityLeaveLevelEvent;
import com.simibubi.create.foundation.event.level.ChunkEvent;
import com.simibubi.create.foundation.event.client.ClientTickEvent;
import com.simibubi.create.foundation.event.tick.LevelTickEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Client half of {@link CreateEventHooks}: Fabric client callbacks → Create's event bridge. Mixin-posted client
 * events (input, camera, fog, hand/arm/player rendering, model baking) live in {@code foundation.mixin.event.client}.
 */
@Environment(EnvType.CLIENT)
public final class CreateClientEventHooks {

	private CreateClientEventHooks() {}

	private static <T extends com.simibubi.create.foundation.event.bus.Event> T post(T event) {
		return CreateEventBus.BUS.post(event);
	}

	public static void register() {
		// ---- ticks ----
		ClientTickEvents.START_CLIENT_TICK.register(mc -> post(new ClientTickEvent.Pre()));
		ClientTickEvents.END_CLIENT_TICK.register(mc -> post(new ClientTickEvent.Post()));
		ClientTickEvents.START_WORLD_TICK.register(level -> post(new LevelTickEvent.Pre(() -> true, level)));
		ClientTickEvents.END_WORLD_TICK.register(level -> post(new LevelTickEvent.Post(() -> true, level)));

		// ---- level / entities ----
		ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> post(new ChunkEvent.Unload(chunk, level)));
		ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> post(new ChunkEvent.Load(chunk, level, false)));
		ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> post(new EntityJoinLevelEvent(entity, level)));
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> post(new EntityLeaveLevelEvent(entity, level)));

		// ---- connection ----
		ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) ->
			post(new ClientPlayerNetworkEvent.LoggingIn(mc.gameMode, mc.player, handler.getConnection())));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) ->
			post(new ClientPlayerNetworkEvent.LoggingOut(mc.gameMode, mc.player, handler.getConnection())));

		// ---- rendering ----
		WorldRenderEvents.AFTER_ENTITIES.register(ctx -> postStage(Stage.AFTER_ENTITIES, ctx));
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((ctx, hit) -> { postStage(Stage.AFTER_BLOCK_ENTITIES, ctx); return true; });
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> postStage(Stage.AFTER_TRANSLUCENT_BLOCKS, ctx));
		WorldRenderEvents.LAST.register(ctx -> {
			// Fabric has no post-particles hook; LAST runs after particles + weather, before the level ends.
			postStage(Stage.AFTER_PARTICLES, ctx);
			postStage(Stage.AFTER_WEATHER, ctx);
		});
		WorldRenderEvents.END.register(ctx -> postStage(Stage.AFTER_LEVEL, ctx));
		WorldRenderEvents.BLOCK_OUTLINE.register((ctx, outline) -> {
			HitResult hit = Minecraft.getInstance().hitResult;
			if (!(hit instanceof BlockHitResult blockHit) || ctx.matrixStack() == null)
				return true;
			RenderHighlightEvent.Block event = post(new RenderHighlightEvent.Block(ctx.worldRenderer(), ctx.camera(), blockHit,
				ctx.tickCounter(), ctx.matrixStack(), ctx.consumers()));
			return !event.isCanceled();
		});
		CoreShaderRegistrationCallback.EVENT.register(context ->
			post(new RegisterShadersEvent(Minecraft.getInstance().getResourceManager(), context)));

		// ---- tooltips ----
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, flag, lines) ->
			post(new com.simibubi.create.foundation.event.entity.player.ItemTooltipEvent(stack, Minecraft.getInstance().player, lines, flag, context)));

		// ---- screens ----
		ScreenEvents.AFTER_INIT.register((mc, screen, scaledWidth, scaledHeight) -> {
			java.util.List<GuiEventListener> listeners = new java.util.ArrayList<>(screen.children());
			post(new ScreenEvent.Init.Post(screen, listeners,
				l -> { if (l instanceof net.minecraft.client.gui.components.AbstractWidget w) Screens.getButtons(screen).add(w); },
				l -> { if (l instanceof net.minecraft.client.gui.components.AbstractWidget w) Screens.getButtons(screen).remove(w); }));
			ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, tickDelta) ->
				post(new ScreenEvent.Render.Post(s, graphics, mouseX, mouseY, tickDelta)));
		});

		// ---- commands ----
		// RegisterClientCommandsEvent is posted per connection by foundation.client.command.ClientCommandHandler
		// (driven from ClientCommandsMixin when the server's command tree arrives).

		// ---- one-shot registration events (each self-registers into the Fabric API on use) ----
		post(new RegisterKeyMappingsEvent());
		post(new RegisterGuiLayersEvent());
		post(new RegisterItemDecorationsEvent());
		post(new RegisterClientReloadListenersEvent());
		post(new RegisterMapDecorationRenderersEvent());
	}

	private static void postStage(Stage stage, WorldRenderContext ctx) {
		if (ctx.matrixStack() == null)
			return;
		post(new RenderLevelStageEvent(stage, ctx.worldRenderer(), ctx.matrixStack(), ctx.positionMatrix(), ctx.projectionMatrix(),
			((com.simibubi.create.foundation.mixin.accessor.LevelRendererAccessor) ctx.worldRenderer()).create$getTicks(), ctx.tickCounter(), ctx.camera(), ctx.frustum()));
	}
}
