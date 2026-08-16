package com.simibubi.create.compat.jei;

import java.util.Collection;
import java.util.List;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.world.level.material.Fluid;

/**
 * JEI ingredient types. On NeoForge JEI ships {@code NeoForgeTypes.FLUID_STACK} for NeoForge's FluidStack; on Fabric
 * it ships {@link FabricTypes#FLUID_STACK} ({@link IJeiFluidIngredient}: a FluidVariant + droplets). Create's JEI
 * compat is written against Create's {@link FluidStack} (mB), so everything converts at the JEI boundary through
 * {@link #toJei}/{@link #fromJei}.
 */
public final class CreateJeiIngredientTypes {

	private CreateJeiIngredientTypes() {}

	public static final IIngredientTypeWithSubtypes<Fluid, IJeiFluidIngredient> FLUID_STACK = FabricTypes.FLUID_STACK;

	/** Droplets per mB (Fabric buckets are 81000 droplets, Create's are 1000 mB). */
	public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000L;

	public static IJeiFluidIngredient toJei(FluidStack stack) {
		return new JeiFluid(FluidVariant.of(stack.getFluid(), stack.getComponentsPatch()), stack.getAmount() * DROPLETS_PER_MB);
	}

	public static List<IJeiFluidIngredient> toJei(Collection<FluidStack> stacks) {
		return stacks.stream().map(CreateJeiIngredientTypes::toJei).toList();
	}

	public static FluidStack fromJei(IJeiFluidIngredient ingredient) {
		FluidVariant variant = ingredient.getFluidVariant();
		return new FluidStack(variant.getFluid().builtInRegistryHolder(), (int) (ingredient.getAmount() / DROPLETS_PER_MB), variant.getComponents());
	}

	/** Capacity/amount for {@code setFluidRenderer} (JEI wants platform units = droplets). */
	public static long toDroplets(int millibuckets) {
		return millibuckets * DROPLETS_PER_MB;
	}

	private record JeiFluid(FluidVariant getFluidVariant, long getAmount) implements IJeiFluidIngredient {}
}
