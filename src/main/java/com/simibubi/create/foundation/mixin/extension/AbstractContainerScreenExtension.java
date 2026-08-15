package com.simibubi.create.foundation.mixin.extension;

/** Injected onto {@code AbstractContainerScreen}; implemented by {@code AbstractContainerScreenMixin}. */
public interface AbstractContainerScreenExtension {

	default int getGuiLeft() {
		throw new AbstractMethodError("not implemented by mixin");
	}

	default int getGuiTop() {
		throw new AbstractMethodError("not implemented by mixin");
	}

	default int getXSize() {
		throw new AbstractMethodError("not implemented by mixin");
	}

	default int getYSize() {
		throw new AbstractMethodError("not implemented by mixin");
	}
}
