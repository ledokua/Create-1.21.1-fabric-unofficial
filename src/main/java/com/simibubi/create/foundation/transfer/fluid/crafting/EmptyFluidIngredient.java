package com.simibubi.create.foundation.transfer.fluid.crafting;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

public class EmptyFluidIngredient extends FluidIngredient {

	public static final EmptyFluidIngredient INSTANCE = new EmptyFluidIngredient();
	public static final MapCodec<EmptyFluidIngredient> CODEC = MapCodec.unit(INSTANCE);
	public static final FluidIngredientType<EmptyFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

	private EmptyFluidIngredient() {}

	@Override
	public boolean test(FluidStack fluidStack) {
		return fluidStack.isEmpty();
	}

	@Override
	protected Stream<FluidStack> generateStacks() {
		return Stream.empty();
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public FluidIngredientType<?> getType() {
		return TYPE;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == INSTANCE;
	}
}
