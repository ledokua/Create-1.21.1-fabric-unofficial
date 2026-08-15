package com.tterrag.registrate.providers;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.builders.Builder;

/** Context bean passed to a {@code setData} datagen callback. Same shape as upstream, unused until Phase 11. */
public class DataGenContext<R, T extends R> {

	public static <R, T extends R> DataGenContext<R, T> from(Builder<R, T, ?, ?> builder) {
		return new DataGenContext<>(builder.getRegistryKey(), builder.getName(), builder);
	}

	private final ResourceKey<? extends Registry<R>> registryKey;
	private final String name;
	private final Builder<R, T, ?, ?> builder;

	private DataGenContext(ResourceKey<? extends Registry<R>> registryKey, String name, Builder<R, T, ?, ?> builder) {
		this.registryKey = registryKey;
		this.name = name;
		this.builder = builder;
	}

	public String getName() {
		return name;
	}

	public T getEntry() {
		return builder.getEntry();
	}

	/** Alias upstream exposes; identical to {@link #getEntry()}. */
	public T get() {
		return getEntry();
	}

	public ResourceLocation getId() {
		return ResourceLocation.fromNamespaceAndPath(builder.getOwner().getModid(), name);
	}
}
