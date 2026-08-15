package com.simibubi.create.foundation.datamap;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

/** Registered {@link DataMapType}s and their loaded values. */
public final class DataMaps {

	private static final Map<DataMapType<?, ?>, Map<Object, Object>> VALUES = new IdentityHashMap<>();
	private static final Map<net.minecraft.resources.ResourceLocation, DataMapType<?, ?>> TYPES = new LinkedHashMap<>();

	private DataMaps() {}

	public static <R, T> DataMapType<R, T> register(DataMapType<R, T> type) {
		TYPES.put(type.id(), type);
		return type;
	}

	public static Iterable<DataMapType<?, ?>> types() {
		return Collections.unmodifiableCollection(TYPES.values());
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <R, T> T get(DataMapType<R, T> type, R entry) {
		Map<Object, Object> map = VALUES.get(type);
		return map == null ? null : (T) map.get(entry);
	}

	@Nullable
	public static <R, T> T get(DataMapType<R, T> type, Holder<R> holder) {
		return get(type, holder.value());
	}

	@SuppressWarnings("unchecked")
	public static <R, T> Map<R, T> view(DataMapType<R, T> type) {
		return (Map<R, T>) Collections.unmodifiableMap(VALUES.getOrDefault(type, Collections.emptyMap()));
	}

	@SuppressWarnings("unchecked")
	static <R, T> void set(DataMapType<R, T> type, Map<R, T> values) {
		VALUES.put(type, (Map<Object, Object>) new IdentityHashMap<>(values));
		if (type.applyHook() != null)
			values.forEach(type.applyHook());
	}

	@SuppressWarnings("unchecked")
	static <R> Registry<R> registryOf(DataMapType<R, ?> type) {
		return (Registry<R>) BuiltInRegistries.REGISTRY.get(type.registryKey().location());
	}

	@SuppressWarnings("unchecked")
	static <R> ResourceKey<Registry<R>> key(DataMapType<R, ?> type) {
		return type.registryKey();
	}
}
