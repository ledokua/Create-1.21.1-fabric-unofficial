package com.simibubi.create.compat.recipeViewer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** Custom rendering for an item slot (mechanical crafter's scaled grid). */
public interface SlotItemRenderer {
	void render(GuiGraphics graphics, ItemStack stack);

	int getWidth();

	int getHeight();
}
