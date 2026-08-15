package com.tterrag.registrate.util.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidEntry<T extends Fluid> extends RegistryEntry<Fluid, T> {

	public static <T extends Fluid> FluidEntry<T> unresolved(ResourceLocation id) {
		return new FluidEntry<>(id);
	}

	protected FluidEntry(ResourceLocation id) {
		super(Registries.FLUID, id);
	}
}
