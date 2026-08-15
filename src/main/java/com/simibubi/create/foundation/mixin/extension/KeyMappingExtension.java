package com.simibubi.create.foundation.mixin.extension;

import com.mojang.blaze3d.platform.InputConstants;

/** Injected onto {@code KeyMapping}; implemented by {@code KeyMappingExtensionMixin}. */
public interface KeyMappingExtension {

	/** NeoForge's {@code KeyMapping#getKey()} — the currently bound key. */
	default InputConstants.Key getKey() {
		throw new AbstractMethodError("not implemented by mixin");
	}

	/** NeoForge's {@code isActiveAndMatches(Key)}; Fabric has no key modifiers, so this is a plain key match. */
	default boolean isActiveAndMatches(InputConstants.Key keyCode) {
		return keyCode != InputConstants.UNKNOWN && keyCode.equals(getKey());
	}
}
