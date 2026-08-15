package com.tterrag.registrate.util.nullness;

import java.util.function.Function;

@FunctionalInterface
public interface NonNullFunction<T, R> extends Function<T, R> {

	@Override
	default <V> NonNullFunction<T, V> andThen(Function<? super R, ? extends V> after) {
		return t -> after.apply(apply(t));
	}
}
