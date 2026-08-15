package com.tterrag.registrate.util.nullness;

import java.util.function.Consumer;

@FunctionalInterface
public interface NonNullConsumer<T> extends Consumer<T> {}
