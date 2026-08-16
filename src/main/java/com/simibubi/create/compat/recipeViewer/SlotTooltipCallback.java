package com.simibubi.create.compat.recipeViewer;

import java.util.List;

import net.minecraft.network.chat.Component;

/** Adds lines to a slot's tooltip (JEI's rich tooltip callback / EMI slot tooltip). */
@FunctionalInterface
public interface SlotTooltipCallback {
	void addTooltip(SlotView view, List<Component> tooltip);
}
