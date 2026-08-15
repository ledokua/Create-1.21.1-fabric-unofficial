package com.simibubi.create.foundation.transfer.fluid.crafting;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

/** Union of child ingredients. Serialized as a JSON list by {@link FluidIngredient#CODEC}. */
public final class CompoundFluidIngredient extends FluidIngredient {

	public static final MapCodec<CompoundFluidIngredient> CODEC = FluidIngredient.LIST_CODEC_NON_EMPTY.fieldOf("children")
		.xmap(CompoundFluidIngredient::new, CompoundFluidIngredient::children);
	public static final FluidIngredientType<CompoundFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

	private final List<FluidIngredient> children;

	public CompoundFluidIngredient(List<? extends FluidIngredient> children) {
		if (children.isEmpty())
			throw new IllegalArgumentException("Compound fluid ingredient must have at least one child");
		this.children = List.copyOf(children);
	}

	public static FluidIngredient of(FluidIngredient... children) {
		return of(List.of(children));
	}

	public static FluidIngredient of(List<FluidIngredient> children) {
		if (children.isEmpty())
			return FluidIngredient.empty();
		if (children.size() == 1)
			return children.get(0);
		return new CompoundFluidIngredient(children);
	}

	public static FluidIngredient of(Stream<FluidIngredient> stream) {
		return of(stream.toList());
	}

	@Override
	public boolean test(FluidStack fluidStack) {
		for (FluidIngredient child : children)
			if (child.test(fluidStack))
				return true;
		return false;
	}

	@Override
	protected Stream<FluidStack> generateStacks() {
		return children.stream().flatMap(child -> Stream.of(child.getStacks()));
	}

	@Override
	public boolean isSimple() {
		return children.stream().allMatch(FluidIngredient::isSimple);
	}

	@Override
	public FluidIngredientType<?> getType() {
		return TYPE;
	}

	@Override
	public int hashCode() {
		return children.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof CompoundFluidIngredient other && other.children.equals(children);
	}

	public List<FluidIngredient> children() {
		return children;
	}
}
