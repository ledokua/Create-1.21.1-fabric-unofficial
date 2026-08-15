package com.simibubi.create.foundation.mixin.extension;

/** Injected onto {@code AbstractMinecart}: NeoForge's slope-adjustment accessor (vanilla's constant). */
public interface AbstractMinecartExtension {
	default double getSlopeAdjustment() {
		return 0.0078125D;
	}
}
