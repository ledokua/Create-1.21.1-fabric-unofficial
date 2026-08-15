package com.simibubi.create.foundation.data.modelgen;

import java.util.function.BiFunction;
import java.util.function.Function;

/** Shell — see package docs. A model + rotation/uvlock/weight, as used in blockstate variants. */
public final class ConfiguredModel {

	public static final int DEFAULT_WEIGHT = 1;

	public final ModelFile model;
	public final int rotationX;
	public final int rotationY;
	public final boolean uvLock;
	public final int weight;

	public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock, int weight) {
		this.model = model;
		this.rotationX = rotationX;
		this.rotationY = rotationY;
		this.uvLock = uvLock;
		this.weight = weight;
	}

	public ConfiguredModel(ModelFile model, int rotationX, int rotationY, boolean uvLock) {
		this(model, rotationX, rotationY, uvLock, DEFAULT_WEIGHT);
	}

	public ConfiguredModel(ModelFile model) {
		this(model, 0, 0, false);
	}

	public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock) {
		return allYRotations(model, x, uvlock, DEFAULT_WEIGHT);
	}

	public static ConfiguredModel[] allYRotations(ModelFile model, int x, boolean uvlock, int weight) {
		return new ConfiguredModel[] { new ConfiguredModel(model, x, 0, uvlock, weight), new ConfiguredModel(model, x, 90, uvlock, weight),
			new ConfiguredModel(model, x, 180, uvlock, weight), new ConfiguredModel(model, x, 270, uvlock, weight) };
	}

	public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock) {
		return allRotations(model, uvlock, DEFAULT_WEIGHT);
	}

	public static ConfiguredModel[] allRotations(ModelFile model, boolean uvlock, int weight) {
		return allYRotations(model, 0, uvlock, weight);
	}

	public static Builder<?> builder() {
		return new Builder<>();
	}

	static <T> Builder<T> builder(Function<ConfiguredModel[], T> callback) {
		return new Builder<>(callback);
	}

	/** Fluent builder; {@code T} is what {@link #addModel()} returns (the enclosing blockstate builder). */
	public static class Builder<T> {
		private final Function<ConfiguredModel[], T> callback;
		private ModelFile model;
		private int rotationX;
		private int rotationY;
		private boolean uvLock;
		private int weight = DEFAULT_WEIGHT;
		private final java.util.List<ConfiguredModel> otherModels = new java.util.ArrayList<>();

		Builder() {
			this(null);
		}

		Builder(Function<ConfiguredModel[], T> callback) {
			this.callback = callback;
		}

		public Builder<T> modelFile(ModelFile model) {
			this.model = model;
			return this;
		}

		public Builder<T> rotationX(int value) {
			rotationX = value;
			return this;
		}

		public Builder<T> rotationY(int value) {
			rotationY = value;
			return this;
		}

		public Builder<T> uvLock(boolean value) {
			uvLock = value;
			return this;
		}

		public Builder<T> weight(int value) {
			weight = value;
			return this;
		}

		public ConfiguredModel buildLast() {
			return new ConfiguredModel(model, rotationX, rotationY, uvLock, weight);
		}

		public ConfiguredModel[] build() {
			otherModels.add(buildLast());
			return otherModels.toArray(new ConfiguredModel[0]);
		}

		public T addModel() {
			return callback == null ? null : callback.apply(build());
		}

		public Builder<T> nextModel() {
			otherModels.add(buildLast());
			return this;
		}
	}
}
