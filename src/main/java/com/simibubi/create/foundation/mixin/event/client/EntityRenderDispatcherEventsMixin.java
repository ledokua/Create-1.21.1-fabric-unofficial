package com.simibubi.create.foundation.mixin.event.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.EntityRenderersEvent;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

/** NeoForge's {@code EntityRenderersEvent.AddLayers}: after renderers are (re)created on resource reload. */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherEventsMixin {
	@Shadow
	private Map<EntityType<?>, EntityRenderer<?>> renderers;

	@Shadow
	public Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers;

	@Inject(method = "onResourceManagerReload", at = @At("TAIL"))
	private void create$addLayers(ResourceManager manager, CallbackInfo ci) {
		CreateEventBus.BUS.post(new EntityRenderersEvent.AddLayers(renderers, playerRenderers));
	}
}
