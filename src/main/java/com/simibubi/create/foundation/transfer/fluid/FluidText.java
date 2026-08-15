package com.simibubi.create.foundation.transfer.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

/**
 * Fluid naming, in one place. Upstream gets names from NeoForge's {@code FluidType}; on Fabric the
 * equivalent registry is {@link FluidVariantAttributes} (which also lets other mods' fluids name themselves
 * correctly). Create's own fluids register a handler there that resolves to the {@code fluid.<ns>.<path>}
 * lang keys upstream ships (see the Phase 2 fluid design in PORTING_PLAN.md).
 */
public final class FluidText {

	private FluidText() {}

	public static String descriptionId(Fluid fluid) {
		return "fluid." + BuiltInRegistries.FLUID.getKey(fluid).toLanguageKey();
	}

	public static Component name(FluidStack stack) {
		if (stack.isEmpty())
			return Component.empty();
		return FluidVariantAttributes.getName(FluidVariant.of(stack.getFluid(), stack.getComponentsPatch()));
	}
}
