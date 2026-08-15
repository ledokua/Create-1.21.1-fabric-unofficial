package com.simibubi.create.foundation.transfer.fluid;

import com.simibubi.create.foundation.transfer.fluid.IFluidHandler.FluidAction;

/** A single tank. */
public interface IFluidTank {

	FluidStack getFluid();

	int getFluidAmount();

	int getCapacity();

	boolean isFluidValid(FluidStack stack);

	int fill(FluidStack resource, FluidAction action);

	FluidStack drain(int maxDrain, FluidAction action);

	FluidStack drain(FluidStack resource, FluidAction action);
}
