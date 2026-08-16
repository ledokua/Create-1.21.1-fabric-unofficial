package com.simibubi.create.foundation.extension;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;

/** Injected onto {@code Font}: NeoForge's {@code ellipsize}. */
public interface FontExtension {

	default FormattedText ellipsize(FormattedText text, int maxWidth) {
		Font font = (Font) this;
		if (font.width(text) <= maxWidth)
			return text;
		String ellipsis = "...";
		int ellipsisWidth = font.width(ellipsis);
		if (ellipsisWidth >= maxWidth)
			return FormattedText.of(ellipsis);
		FormattedText trimmed = font.substrByWidth(text, maxWidth - ellipsisWidth);
		return FormattedText.composite(trimmed, FormattedText.of(ellipsis));
	}

}
