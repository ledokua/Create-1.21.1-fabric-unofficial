package com.simibubi.create.foundation.event.client;

import java.util.List;

import com.simibubi.create.foundation.event.bus.Event;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;

/** Tooltip rendering hooks; fired from a GuiGraphics#renderTooltipInternal mixin. */
public abstract class RenderTooltipEvent extends Event {

	protected final ItemStack itemStack;
	protected final GuiGraphics graphics;
	protected int x;
	protected int y;
	protected Font font;
	protected final List<ClientTooltipComponent> components;

	protected RenderTooltipEvent(ItemStack itemStack, GuiGraphics graphics, int x, int y, Font font, List<ClientTooltipComponent> components) {
		this.itemStack = itemStack;
		this.graphics = graphics;
		this.x = x;
		this.y = y;
		this.font = font;
		this.components = components;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public GuiGraphics getGraphics() {
		return graphics;
	}

	public List<ClientTooltipComponent> getComponents() {
		return components;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Font getFont() {
		return font;
	}

	/** Before layout; handlers may move the tooltip, swap the font, or cancel it. */
	public static class Pre extends RenderTooltipEvent implements ICancellableEvent {
		private final int screenWidth;
		private final int screenHeight;
		private final ClientTooltipPositioner positioner;

		public Pre(ItemStack stack, GuiGraphics graphics, int x, int y, int screenWidth, int screenHeight, Font font, List<ClientTooltipComponent> components, ClientTooltipPositioner positioner) {
			super(stack, graphics, x, y, font, components);
			this.screenWidth = screenWidth;
			this.screenHeight = screenHeight;
			this.positioner = positioner;
		}

		public int getScreenWidth() {
			return screenWidth;
		}

		public int getScreenHeight() {
			return screenHeight;
		}

		public ClientTooltipPositioner getTooltipPositioner() {
			return positioner;
		}

		public void setFont(Font fr) {
			this.font = fr;
		}

		public void setX(int x) {
			this.x = x;
		}

		public void setY(int y) {
			this.y = y;
		}
	}

	/** Tooltip background/border colours. */
	public static class Color extends RenderTooltipEvent {
		private final int originalBackground;
		private final int originalBorderStart;
		private final int originalBorderEnd;
		private int backgroundStart;
		private int backgroundEnd;
		private int borderStart;
		private int borderEnd;

		public Color(ItemStack stack, GuiGraphics graphics, int x, int y, Font font, int background, int borderStart, int borderEnd, List<ClientTooltipComponent> components) {
			super(stack, graphics, x, y, font, components);
			this.originalBackground = background;
			this.originalBorderStart = borderStart;
			this.originalBorderEnd = borderEnd;
			this.backgroundStart = background;
			this.backgroundEnd = background;
			this.borderStart = borderStart;
			this.borderEnd = borderEnd;
		}

		public int getBackgroundStart() {
			return backgroundStart;
		}

		public int getBackgroundEnd() {
			return backgroundEnd;
		}

		public void setBackground(int background) {
			this.backgroundStart = background;
			this.backgroundEnd = background;
		}

		public void setBackgroundStart(int backgroundStart) {
			this.backgroundStart = backgroundStart;
		}

		public void setBackgroundEnd(int backgroundEnd) {
			this.backgroundEnd = backgroundEnd;
		}

		public int getBorderStart() {
			return borderStart;
		}

		public void setBorderStart(int borderStart) {
			this.borderStart = borderStart;
		}

		public int getBorderEnd() {
			return borderEnd;
		}

		public void setBorderEnd(int borderEnd) {
			this.borderEnd = borderEnd;
		}

		public int getOriginalBackground() {
			return originalBackground;
		}

		public int getOriginalBorderStart() {
			return originalBorderStart;
		}

		public int getOriginalBorderEnd() {
			return originalBorderEnd;
		}
	}
}
