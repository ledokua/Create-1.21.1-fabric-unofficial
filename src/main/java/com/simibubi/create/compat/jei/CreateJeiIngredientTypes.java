package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.minecraft.world.level.material.Fluid;

/**
 * JEI ingredient types. On NeoForge JEI ships {@code NeoForgeTypes.FLUID_STACK} for NeoForge's FluidStack;
 * on Fabric it ships an {@code IJeiFluidIngredient} type instead. Create's JEI compat is written against a
 * FluidStack-typed ingredient, so we register our own (Phase 12 registers the matching helper/renderer in
 * CreateJEI; until then this only satisfies the type references).
 */
public final class CreateJeiIngredientTypes {

	private CreateJeiIngredientTypes() {}

	public static final IIngredientTypeWithSubtypes<Fluid, FluidStack> FLUID_STACK = new IIngredientTypeWithSubtypes<>() {
		@Override
		public Class<? extends FluidStack> getIngredientClass() {
			return FluidStack.class;
		}

		@Override
		public Class<? extends Fluid> getIngredientBaseClass() {
			return Fluid.class;
		}

		@Override
		public Fluid getBase(FluidStack ingredient) {
			return ingredient.getFluid();
		}
	};
}
