package com.simibubi.create.foundation.event.client;

import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.resources.PlayerSkin;

/** Lifecycle events around entity renderers. */
public abstract class EntityRenderersEvent extends Event {

	/**
	 * Add feature layers to existing renderers. Constructed by CreateEventHooks with the live dispatcher
	 * once renderers exist (Fabric: LivingEntityFeatureRendererRegistrationCallback / after-init hook).
	 */
	public static class AddLayers extends EntityRenderersEvent {
		private final Map<EntityType<?>, EntityRenderer<?>> renderers;
		private final Map<PlayerSkin.Model, EntityRenderer<? extends net.minecraft.world.entity.player.Player>> skinMap;

		public AddLayers(Map<EntityType<?>, EntityRenderer<?>> renderers, Map<PlayerSkin.Model, EntityRenderer<? extends net.minecraft.world.entity.player.Player>> skinMap) {
			this.renderers = renderers;
			this.skinMap = skinMap;
		}

		public Set<PlayerSkin.Model> getSkins() {
			return skinMap.keySet();
		}

		@Nullable
		@SuppressWarnings("unchecked")
		public <R extends LivingEntityRenderer<? extends AbstractClientPlayer, ? extends PlayerModel<? extends AbstractClientPlayer>>> R getSkin(PlayerSkin.Model skin) {
			return (R) skinMap.get(skin);
		}

		@Nullable
		public PlayerRenderer getPlayerSkin(PlayerSkin.Model skin) {
			return (PlayerRenderer) skinMap.get(skin);
		}

		@Nullable
		@SuppressWarnings("unchecked")
		public <T extends LivingEntity, R extends LivingEntityRenderer<T, ? extends EntityModel<T>>> R getRenderer(EntityType<? extends T> entityType) {
			return (R) renderers.get(entityType);
		}

		public EntityRenderDispatcher getDispatcher() {
			return net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher();
		}
	}
}
