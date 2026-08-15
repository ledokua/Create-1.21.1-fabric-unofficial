package com.simibubi.create.foundation.mixin.extension;

import net.minecraft.client.Minecraft;

/** Injected onto {@code Screen}: NeoForge's {@code getMinecraft()}. */
public interface ScreenExtension {
	default Minecraft getMinecraft() {
		return Minecraft.getInstance();
	}
}
