package com.simibubi.create.foundation.model.data;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

/**
 * Immutable typed map of per-position model data handed to block models at render time. Create-owned (was
 * NeoForge's). On Fabric it travels as the block entity's render data ({@code RenderDataBlockEntity}) and is
 * read back by {@link com.simibubi.create.foundation.model.BakedModelWrapper}'s FRAPI bridge.
 */
public final class ModelData {

	public static final ModelData EMPTY = new ModelData(Collections.emptyMap());

	private final Map<ModelProperty<?>, Object> properties;

	private ModelData(Map<ModelProperty<?>, Object> properties) {
		this.properties = properties;
	}

	public Set<ModelProperty<?>> getProperties() {
		return properties.keySet();
	}

	public boolean has(ModelProperty<?> property) {
		return properties.containsKey(property);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T get(ModelProperty<T> property) {
		return (T) properties.get(property);
	}

	public Builder derive() {
		return new Builder(this);
	}

	public static Builder builder() {
		return new Builder(null);
	}

	public static final class Builder {
		private final Map<ModelProperty<?>, Object> properties = new IdentityHashMap<>();

		private Builder(@Nullable ModelData parent) {
			if (parent != null)
				properties.putAll(parent.properties);
		}

		public <T> Builder with(ModelProperty<T> property, T value) {
			if (!property.test(value))
				throw new IllegalArgumentException("Value " + value + " is invalid for property " + property);
			properties.put(property, value);
			return this;
		}

		public ModelData build() {
			return properties.isEmpty() ? EMPTY : new ModelData(Collections.unmodifiableMap(properties));
		}
	}
}
