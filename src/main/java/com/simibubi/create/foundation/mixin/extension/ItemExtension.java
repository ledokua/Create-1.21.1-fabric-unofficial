package com.simibubi.create.foundation.mixin.extension;

import java.util.function.Consumer;

import com.simibubi.create.foundation.client.extensions.IClientItemExtensions;

/** Injected onto {@code Item}: NeoForge's client-extension registration hook Create's items override. */
public interface ItemExtension {
	default void initializeClient(Consumer<IClientItemExtensions> consumer) {}
}
