package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import mezz.jei.api.ingredients.IIngredientType;

/**
 * JEI ingredient types. On NeoForge JEI ships {@code CreateJeiIngredientTypes.FLUID_STACK} for NeoForge's FluidStack;
 * on Fabric it ships an {@code IJeiFluidIngredient} type instead. Create's JEI compat is written against a
 * FluidStack-typed ingredient, so we register our own (Phase 12 registers the matching helper/renderer in
 * CreateJEI; until then this only satisfies the type references).
 */
public final class CreateJeiIngredientTypes {

	private CreateJeiIngredientTypes() {}

	public static final IIngredientType<FluidStack> FLUID_STACK = () -> FluidStack.class;
}
