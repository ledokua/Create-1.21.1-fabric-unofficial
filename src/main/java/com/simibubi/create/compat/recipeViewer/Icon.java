package com.simibubi.create.compat.recipeViewer;

import net.minecraft.client.gui.GuiGraphics;

/** Something drawable at a fixed size (JEI Icon / EMI EmiRenderable). Category icons, backgrounds, animations. */
public interface Icon {
	int getWidth();

	int getHeight();

	void draw(GuiGraphics graphics, int xOffset, int yOffset);
}
