package com.simibubi.create.foundation.data.modelgen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;

/** Shell — see package docs. Multipart-style blockstate JSON builder; nothing is recorded. */
public class MultiPartBlockStateBuilder {

	private final Block owner;

	MultiPartBlockStateBuilder(Block owner) {
		this.owner = owner;
	}

	public Block getOwner() {
		return owner;
	}

	public ConfiguredModel.Builder<PartBuilder> part() {
		return ConfiguredModel.builder(models -> new PartBuilder(this));
	}

	public MultiPartBlockStateBuilder addPart(PartBuilder part) {
		return this;
	}

	public class PartBuilder {
		private final MultiPartBlockStateBuilder outerBuilder;

		PartBuilder(MultiPartBlockStateBuilder outerBuilder) {
			this.outerBuilder = outerBuilder;
		}

		public PartBuilder useOr() {
			return this;
		}

		@SafeVarargs
		public final <T extends Comparable<T>> PartBuilder condition(Property<T> prop, T... values) {
			return this;
		}

		public PartBuilder nestedGroup() {
			return this;
		}

		public PartBuilder endNestedGroup() {
			return this;
		}

		public MultiPartBlockStateBuilder end() {
			return outerBuilder;
		}
	}
}
