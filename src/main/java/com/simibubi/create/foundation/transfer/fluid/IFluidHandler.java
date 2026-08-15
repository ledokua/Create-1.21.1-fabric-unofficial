package com.simibubi.create.foundation.transfer.fluid;

/**
 * Tank-based fluid storage abstraction Create's fluid systems are written against. Create-owned port of the
 * contract upstream gets from NeoForge (see PORTING_PLAN.md, "NeoForge → Fabric translation strategy");
 * bridged to Fabric's Transfer API at lookup boundaries by
 * {@link com.simibubi.create.foundation.transfer.capability.Capabilities}. Amounts are millibuckets.
 * <p>
 * Contract: returned stacks must not be mutated by callers; {@link FluidAction#SIMULATE} calls have no side
 * effects.
 */
public interface IFluidHandler {

	enum FluidAction {
		EXECUTE, SIMULATE;

		public boolean execute() {
			return this == EXECUTE;
		}

		public boolean simulate() {
			return this == SIMULATE;
		}
	}

	int getTanks();

	/** The contents of the tank — a live reference for cheap inspection; do NOT modify it. */
	FluidStack getFluidInTank(int tank);

	int getTankCapacity(int tank);

	boolean isFluidValid(int tank, FluidStack stack);

	/** @return the amount that was (or would be) filled */
	int fill(FluidStack resource, FluidAction action);

	/** Drains fluid matching {@code resource} (fluid + components), up to its amount. */
	FluidStack drain(FluidStack resource, FluidAction action);

	/** Drains up to {@code maxDrain} of whatever fluid is available. */
	FluidStack drain(int maxDrain, FluidAction action);
}
