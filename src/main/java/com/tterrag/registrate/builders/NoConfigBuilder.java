package com.tterrag.registrate.builders;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/** Builder for entries with no type-specific configuration — just a factory. Same as upstream. */
public class NoConfigBuilder<R, T extends R, P> extends AbstractBuilder<R, T, P, NoConfigBuilder<R, T, P>> {

	private final NonNullSupplier<T> factory;

	public NoConfigBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		ResourceKey<? extends Registry<R>> registryKey, NonNullSupplier<T> factory) {
		super(owner, parent, name, callback, registryKey);
		this.factory = factory;
	}

	@Override
	protected T createEntry() {
		return factory.get();
	}
}
