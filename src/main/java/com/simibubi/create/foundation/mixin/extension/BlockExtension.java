package com.simibubi.create.foundation.mixin.extension;

import java.util.function.Consumer;

import com.simibubi.create.foundation.client.extensions.IClientBlockExtensions;

/** Injected onto {@code Block}: NeoForge's client-extension registration hook Create's blocks override. */
public interface BlockExtension {
	default void initializeClient(Consumer<IClientBlockExtensions> consumer) {}
}
