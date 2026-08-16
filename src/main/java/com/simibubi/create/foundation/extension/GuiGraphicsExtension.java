package com.simibubi.create.foundation.extension;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/** Injected onto {@code GuiGraphics}: NeoForge's float-positioned {@code drawString} overloads. */
public interface GuiGraphicsExtension {

	private GuiGraphics self() {
		return (GuiGraphics) this;
	}

	default int drawString(Font font, String text, float x, float y, int color, boolean dropShadow) {
		return text == null ? 0 : drawString(font, FormattedCharSequence.forward(text, net.minecraft.network.chat.Style.EMPTY), x, y, color, dropShadow);
	}

	default int drawString(Font font, Component text, float x, float y, int color, boolean dropShadow) {
		return drawString(font, text.getVisualOrderText(), x, y, color, dropShadow);
	}

	default int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
		GuiGraphics g = self();
		int result = font.drawInBatch(text, x, y, color, dropShadow, g.pose().last().pose(), g.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
		g.flush();
		return result;
	}
}
