package com.simibubi.create.foundation.mixin.extension;

import com.simibubi.create.foundation.transfer.fluid.FluidType;
import com.simibubi.create.foundation.transfer.fluid.FluidTypeRegistry;

import net.minecraft.world.level.material.FluidState;

/** Injected onto {@code FluidState}: NeoForge's {@code FluidState#getFluidType()}. */
public interface FluidStateExtension {
	default FluidType getFluidType() {
		return FluidTypeRegistry.get(((FluidState) this).getType());
	}
}
