package com.simibubi.create.foundation.transfer.fluid.crafting;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/** JSON: {@code {"fluid": "ns:id"}} (optionally with {@code "type": "create:single"}). */
public class SingleFluidIngredient extends FluidIngredient {

	public static final MapCodec<SingleFluidIngredient> CODEC = FluidStack.FLUID_NON_EMPTY_CODEC
		.xmap(SingleFluidIngredient::new, SingleFluidIngredient::fluid).fieldOf("fluid");
	public static final FluidIngredientType<SingleFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

	private final Holder<Fluid> fluid;

	public SingleFluidIngredient(Holder<Fluid> fluid) {
		if (fluid.is(Fluids.EMPTY.builtInRegistryHolder()))
			throw new IllegalStateException("SingleFluidIngredient must not be constructed with minecraft:empty, use FluidIngredient.empty() instead!");
		this.fluid = fluid;
	}

	@Override
	public boolean test(FluidStack fluidStack) {
		return fluidStack.is(fluid);
	}

	@Override
	protected Stream<FluidStack> generateStacks() {
		return Stream.of(new FluidStack(fluid, FluidStack.BUCKET_VOLUME));
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
		return fluid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof SingleFluidIngredient other && other.fluid.equals(fluid);
	}

	public Holder<Fluid> fluid() {
		return fluid;
	}
}
