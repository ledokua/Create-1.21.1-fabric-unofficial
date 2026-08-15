package com.tterrag.registrate.util.nullness;

import java.util.function.Supplier;

/**
 * A {@link Supplier} whose result is guaranteed non-null. Reimplemented for the Fabric port — same
 * name/shape as upstream Registrate so ported Create code needs no import changes.
 */
@FunctionalInterface
public interface NonNullSupplier<T> extends Supplier<T> {

	static <T> NonNullSupplier<T> lazy(NonNullSupplier<T> supplier) {
		return new NonNullSupplier<>() {
			private T value;
			private boolean resolved;

			@Override
			public T get() {
				if (!resolved) {
					value = supplier.get();
					resolved = true;
				}
				return value;
			}
		};
	}
}
