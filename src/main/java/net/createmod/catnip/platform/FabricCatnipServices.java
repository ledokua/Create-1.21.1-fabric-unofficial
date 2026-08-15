package net.createmod.catnip.platform;

import net.createmod.catnip.platform.services.ModFluidHelper;
import net.createmod.catnip.render.FluidRenderHelper;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

@SuppressWarnings("unchecked")
public class FabricCatnipServices {
	public static final ModFluidHelper<FluidVariant> FLUID_HELPER = (ModFluidHelper<FluidVariant>) CatnipServices.FLUID_HELPER;
	public static final FluidRenderHelper<FluidVariant> FLUID_RENDERER = (FluidRenderHelper<FluidVariant>) CatnipServices.FLUID_RENDERER;
}
