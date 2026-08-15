package com.simibubi.create.foundation.transfer.fluid;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Fluid → {@link FluidType} mapping behind {@code Fluid#getFluidType()} (interface-injected). Create's own
 * fluids bind here at construction; vanilla water/lava/empty map to {@link CommonFluids}; anything else gets a
 * generic type synthesised from Fabric's {@link FluidVariantAttributes} so foreign fluids still work in
 * Create's pipes/tanks. Also publishes Create's types *to* {@link FluidVariantAttributes}.
 */
public final class FluidTypeRegistry {

	private static final Map<Fluid, Supplier<? extends FluidType>> TYPES = new IdentityHashMap<>();
	private static final Map<Fluid, FluidType> FOREIGN = new IdentityHashMap<>();

	private FluidTypeRegistry() {}

	static void bind(Fluid fluid, Supplier<? extends FluidType> type) {
		TYPES.put(fluid, type);
	}

	public static FluidType get(Fluid fluid) {
		Supplier<? extends FluidType> own = TYPES.get(fluid);
		if (own != null)
			return own.get();
		if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER)
			return CommonFluids.WATER_TYPE.value();
		if (fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA)
			return CommonFluids.LAVA_TYPE.value();
		if (fluid == Fluids.EMPTY)
			return CommonFluids.EMPTY_TYPE.value();
		return FOREIGN.computeIfAbsent(fluid, FluidTypeRegistry::synthesize);
	}

	private static FluidType synthesize(Fluid fluid) {
		FluidVariant variant = FluidVariant.of(fluid);
		FluidVariantAttributeHandler handler = FluidVariantAttributes.getHandlerOrDefault(fluid);
		int temperature = handler.getTemperature(variant);
		int viscosity = handler.getViscosity(variant, null);
		int luminance = handler.getLuminance(variant);
		boolean lighter = handler.isLighterThanAir(variant);
		return new FluidType(FluidType.Properties.create()
			.descriptionId("fluid." + BuiltInRegistries.FLUID.getKey(fluid).toLanguageKey())
			.temperature(temperature)
			.viscosity(viscosity)
			.lightLevel(luminance)
			.density(lighter ? -1000 : 1000)) {
			@Override
			public Component getDescription() {
				return handler.getName(variant);
			}
		};
	}

	/**
	 * Called after registration for each of Create's fluids: publishes the {@link FluidType}'s data to
	 * Fabric's fluid attribute registry so other mods see the right name/temperature/viscosity/luminance.
	 */
	public static void publishToFabric(Fluid fluid, FluidType type) {
		if (fluid == Fluids.EMPTY)
			return;
		FluidVariantAttributes.register(fluid, new FluidVariantAttributeHandler() {
			@Override
			public Component getName(FluidVariant fluidVariant) {
				return type.getDescription(new FluidStack(fluidVariant.getFluid().builtInRegistryHolder(), FluidType.BUCKET_VOLUME, fluidVariant.getComponents()));
			}

			@Override
			public java.util.Optional<SoundEvent> getFillSound(FluidVariant variant) {
				return java.util.Optional.ofNullable(type.getSound(SoundActions.BUCKET_FILL));
			}

			@Override
			public java.util.Optional<SoundEvent> getEmptySound(FluidVariant variant) {
				return java.util.Optional.ofNullable(type.getSound(SoundActions.BUCKET_EMPTY));
			}

			@Override
			public int getLuminance(FluidVariant variant) {
				return type.getLightLevel();
			}

			@Override
			public int getTemperature(FluidVariant variant) {
				return type.getTemperature();
			}

			@Override
			public int getViscosity(FluidVariant variant, @Nullable net.minecraft.world.level.Level world) {
				return type.getViscosity();
			}

			@Override
			public boolean isLighterThanAir(FluidVariant variant) {
				return type.isLighterThanAir();
			}
		});
	}
}
