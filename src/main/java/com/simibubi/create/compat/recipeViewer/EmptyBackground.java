package com.simibubi.create.compat.recipeViewer;

import com.simibubi.create.compat.recipeViewer.Icon;
import net.minecraft.client.gui.GuiGraphics;

public class EmptyBackground implements Icon {

	private int width;
	private int height;

	public EmptyBackground(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {}

}
