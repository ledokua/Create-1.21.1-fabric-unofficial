package com.simibubi.create.foundation.extension;

import com.simibubi.create.foundation.transfer.fluid.FluidType;
import com.simibubi.create.foundation.transfer.fluid.FluidTypeRegistry;

import net.minecraft.world.level.material.Fluid;

/** Injected onto {@code Fluid}: NeoForge's {@code Fluid#getFluidType()}. */
public interface FluidExtension {
	default FluidType getFluidType() {
		return FluidTypeRegistry.get((Fluid) this);
	}
}
