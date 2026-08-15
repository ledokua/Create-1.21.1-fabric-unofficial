package com.simibubi.create.compat.trainmap;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.compat.Mods;

import net.minecraft.client.Minecraft;

import net.fabricmc.api.EnvType;
import com.simibubi.create.foundation.event.bus.SubscribeEvent;
import com.simibubi.create.foundation.event.bus.EventBusSubscriber;
import com.simibubi.create.foundation.event.client.ClientTickEvent;
import com.simibubi.create.foundation.event.client.InputEvent;
import com.simibubi.create.foundation.event.client.RenderTooltipEvent;
import com.simibubi.create.foundation.event.client.ScreenEvent;

@EventBusSubscriber(value = EnvType.CLIENT)
public class TrainMapEvents {

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;

		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.tick();
		if (Mods.JOURNEYMAP.isLoaded())
			JourneyTrainMap.tick();
		if (Mods.XAEROWORLDMAP.isLoaded())
			XaeroTrainMap.tick();
	}

	@SubscribeEvent
	public static void mouseClick(InputEvent.MouseButton.Pre event) {
		if (event.getAction() != InputConstants.PRESS)
			return;

		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.mouseClick(event);
		if (Mods.JOURNEYMAP.isLoaded())
			JourneyTrainMap.mouseClick(event);
		if (Mods.XAEROWORLDMAP.isLoaded())
			XaeroTrainMap.mouseClick(event);
	}

	@SubscribeEvent
	public static void cancelTooltips(RenderTooltipEvent.Pre event) {
		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.cancelTooltips(event);
	}

	@SubscribeEvent
	public static void renderGui(ScreenEvent.Render.Post event) {
		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.renderGui(event);
	}
}
