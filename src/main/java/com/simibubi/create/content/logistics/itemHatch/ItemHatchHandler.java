package com.simibubi.create.content.logistics.itemHatch;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;

import com.simibubi.create.foundation.event.bus.EventPriority;
import com.simibubi.create.foundation.event.bus.SubscribeEvent;
import com.simibubi.create.foundation.event.bus.EventBusSubscriber;
import net.fabricmc.fabric.api.util.TriState;
import com.simibubi.create.foundation.event.entity.player.PlayerInteractEvent.RightClickBlock;


@EventBusSubscriber(modid = Create.ID)
public class ItemHatchHandler {

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void useOnItemHatchIgnoresSneak(RightClickBlock event) {
		if (event.getUseItem() == TriState.DEFAULT && AllBlocks.ITEM_HATCH.has(event.getLevel()
			.getBlockState(event.getPos())))
			event.setUseBlock(TriState.TRUE);
	}

}
