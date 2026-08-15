package com.tterrag.registrate.util.nullness;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface NonNullBiConsumer<T, U> extends BiConsumer<T, U> {

	static <T, U> NonNullBiConsumer<T, U> noop() {
		return (t, u) -> {};
	}
}
