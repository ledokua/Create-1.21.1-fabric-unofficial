package com.simibubi.create.foundation.event.client;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Lifecycle event: per-item overlays drawn on top of GUI item stacks (NeoForge item decorators). Applied by
 * a GuiGraphics#renderItemDecorations mixin.
 */
public class RegisterItemDecorationsEvent extends Event {

	private static final Map<net.minecraft.world.item.Item, List<IItemDecorator>> DECORATORS = new IdentityHashMap<>();

	public void register(ItemLike item, IItemDecorator decorator) {
		DECORATORS.computeIfAbsent(item.asItem(), $ -> new ArrayList<>()).add(decorator);
	}

	public static List<IItemDecorator> decoratorsFor(net.minecraft.world.item.Item item) {
		return DECORATORS.getOrDefault(item, List.of());
	}

	@FunctionalInterface
	public interface IItemDecorator {
		/** @return true if render state (e.g. blend, depth) was changed and vanilla should reset it */
		boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset);
	}
}
