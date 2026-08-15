package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * A supplier for a builder's entry that drops its reference to the builder once resolved (so lambdas that
 * capture it don't keep the whole builder — and everything it captured — alive for the game's lifetime).
 * Same purpose as upstream's class of the same name.
 */
public class LazyRegistryEntry<R, T extends R> implements NonNullSupplier<T> {

	private Builder<R, T, ?, ?> builder;
	private RegistryEntry<R, T> entry;

	public LazyRegistryEntry(Builder<R, T, ?, ?> builder) {
		this.builder = builder;
	}

	@Override
	public T get() {
		if (entry == null) {
			entry = builder.get();
			builder = null;
		}
		return entry.get();
	}
}
