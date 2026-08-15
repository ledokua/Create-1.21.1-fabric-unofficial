package com.tterrag.registrate.util.nullness;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface NonNullUnaryOperator<T> extends UnaryOperator<T>, NonNullFunction<T, T> {}
