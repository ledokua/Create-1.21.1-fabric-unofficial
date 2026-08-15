package com.simibubi.create.foundation.data.modelgen;

import java.util.function.Function;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/** Shell — see package docs. Variant-style blockstate JSON builder; nothing is recorded. */
public class VariantBlockStateBuilder {

	private final Block owner;

	VariantBlockStateBuilder(Block owner) {
		this.owner = owner;
	}

	public Block getOwner() {
		return owner;
	}

	public PartialBlockstate partialState() {
		return new PartialBlockstate(this);
	}

	public VariantBlockStateBuilder addModels(PartialBlockstate state, ConfiguredModel... models) {
		return this;
	}

	public VariantBlockStateBuilder setModels(PartialBlockstate state, ConfiguredModel... model) {
		return this;
	}

	public VariantBlockStateBuilder forAllStates(Function<BlockState, ConfiguredModel[]> mapper) {
		return this;
	}

	public VariantBlockStateBuilder forAllStatesExcept(Function<BlockState, ConfiguredModel[]> mapper, Property<?>... ignored) {
		return this;
	}

	public static class PartialBlockstate {
		private final VariantBlockStateBuilder outerBuilder;

		PartialBlockstate(VariantBlockStateBuilder outerBuilder) {
			this.outerBuilder = outerBuilder;
		}

		public <T extends Comparable<T>> PartialBlockstate with(Property<T> prop, T value) {
			return this;
		}

		public VariantBlockStateBuilder addModels(ConfiguredModel... models) {
			return outerBuilder;
		}

		public VariantBlockStateBuilder setModels(ConfiguredModel... models) {
			return outerBuilder;
		}

		public ConfiguredModel.Builder<VariantBlockStateBuilder> modelForState() {
			return ConfiguredModel.builder(models -> outerBuilder);
		}

		public Block getOwner() {
			return outerBuilder.getOwner();
		}
	}
}
