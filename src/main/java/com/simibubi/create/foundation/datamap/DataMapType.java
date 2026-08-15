package com.simibubi.create.foundation.datamap;

import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * A registry-attached data map (NeoForge's concept, Create-owned): JSON files under
 * {@code data/<ns>/data_maps/<registry>/<id>.json} attach a value to registry entries or tags. Loaded by
 * {@link DataMapLoader}; queried through {@code Holder#getData(type)} (interface-injected). Types register in
 * {@link DataMaps}. An optional {@link #applyHook} pushes values into a Fabric registry (fuel, oxidizables).
 */
public final class DataMapType<R, T> {

	public static <R, T> Builder<R, T> builder(ResourceLocation id, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
		return new Builder<>(id, registryKey, codec);
	}

	private final ResourceLocation id;
	private final ResourceKey<Registry<R>> registryKey;
	private final Codec<T> codec;
	@Nullable
	private final BiConsumer<R, T> applyHook;

	private DataMapType(ResourceLocation id, ResourceKey<Registry<R>> registryKey, Codec<T> codec, @Nullable BiConsumer<R, T> applyHook) {
		this.id = id;
		this.registryKey = registryKey;
		this.codec = codec;
		this.applyHook = applyHook;
	}

	public ResourceLocation id() {
		return id;
	}

	public ResourceKey<Registry<R>> registryKey() {
		return registryKey;
	}

	public Codec<T> codec() {
		return codec;
	}

	@Nullable
	public BiConsumer<R, T> applyHook() {
		return applyHook;
	}

	@Override
	public String toString() {
		return "DataMapType[" + id + " @ " + registryKey.location() + "]";
	}

	public static final class Builder<R, T> {
		private final ResourceLocation id;
		private final ResourceKey<Registry<R>> registryKey;
		private final Codec<T> codec;
		@Nullable
		private BiConsumer<R, T> applyHook;

		private Builder(ResourceLocation id, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
			this.id = id;
			this.registryKey = registryKey;
			this.codec = codec;
		}

		/** Called for every entry after (re)load — bridge into Fabric registries. */
		public Builder<R, T> applyHook(BiConsumer<R, T> hook) {
			this.applyHook = hook;
			return this;
		}

		public DataMapType<R, T> build() {
			return new DataMapType<>(id, registryKey, codec, applyHook);
		}
	}
}
