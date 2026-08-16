package com.simibubi.create.compat.sodium;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.RenderLevelStageEvent;
import com.simibubi.create.foundation.event.client.RenderLevelStageEvent.Stage;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Fixes the Mechanical Saw's and Factory Gauge's animated sprites under Sodium (which only ticks sprites it saw
 * used by chunk meshes): every frame, mark them active through Sodium's {@code SpriteUtil}. Sodium 0.6+ ships the
 * same {@code net.caffeinemc.mods.sodium.api.texture.SpriteUtil} on Fabric and NeoForge; reached via method handles
 * so no compile dependency on Sodium is needed.
 */
@Environment(EnvType.CLIENT)
public class SodiumCompat {
	public static final ResourceLocation SAW_TEXTURE = Create.asResource("block/saw_reversed");
	public static final ResourceLocation FACTORY_PANEL_TEXTURE = Create.asResource("block/factory_panel_connections_animated");

	public static void init() {
		MethodHandle markSpriteActive;
		try {
			Class<?> spriteUtil = Class.forName("net.caffeinemc.mods.sodium.api.texture.SpriteUtil");
			Object instance = spriteUtil.getField("INSTANCE").get(null);
			markSpriteActive = MethodHandles.publicLookup()
				.findVirtual(spriteUtil, "markSpriteActive", MethodType.methodType(void.class, TextureAtlasSprite.class))
				.bindTo(instance);
		} catch (ReflectiveOperationException | RuntimeException e) {
			Create.LOGGER.warn("Sodium is present but its SpriteUtil API could not be bound; animated saw/factory gauge sprites may freeze", e);
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		CreateEventBus.BUS.addListener(RenderLevelStageEvent.class, (RenderLevelStageEvent event) -> {
			if (event.getStage() != Stage.AFTER_ENTITIES)
				return;
			Function<ResourceLocation, TextureAtlasSprite> atlas = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
			try {
				markSpriteActive.invoke(atlas.apply(SAW_TEXTURE));
				markSpriteActive.invoke(atlas.apply(FACTORY_PANEL_TEXTURE));
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		});
	}
}
