package com.tterrag.registrate.util.nullness;

import java.util.function.Supplier;

@FunctionalInterface
public interface NullableSupplier<T> extends Supplier<T> {}
