package com.simibubi.create.foundation.transfer.fluid;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.registry.DeferredHolder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Stand-in for the fluid-related statics upstream takes from NeoForge's {@code NeoForgeMod}: fluid types for
 * vanilla water/lava/empty, and a <b>milk</b> fluid ({@code create:milk}) — Fabric has no standard milk
 * fluid, so Create provides one (tagged {@code c:milk} through vendored data, matching what upstream recipes
 * expect). Registered by {@link #enableMilkFluid()} from Create's init, mirroring upstream's call.
 */
public final class CommonFluids {

	private CommonFluids() {}

	public static final Holder<FluidType> EMPTY_TYPE = Holder.direct(new FluidType(FluidType.Properties.create()
		.descriptionId("block.minecraft.air").motionScale(1D).canPushEntity(false).canSwim(false).canDrown(false)
		.fallDistanceModifier(1F).pathType(null).adjacentPathType(null).density(0).temperature(0).viscosity(0)));

	public static final Holder<FluidType> WATER_TYPE = Holder.direct(new FluidType(FluidType.Properties.create()
		.descriptionId("block.minecraft.water").fallDistanceModifier(0F).canExtinguish(true).canConvertToSource(true)
		.supportsBoating(true).sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
		.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY).sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)
		.canHydrate(true)));

	public static final Holder<FluidType> LAVA_TYPE = Holder.direct(new FluidType(FluidType.Properties.create()
		.descriptionId("block.minecraft.lava").canSwim(false).canDrown(false).pathType(PathType.LAVA).adjacentPathType(null)
		.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL_LAVA).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
		.lightLevel(15).density(3000).viscosity(6000).temperature(1300)));

	public static final Holder<FluidType> MILK_TYPE = Holder.direct(new FluidType(FluidType.Properties.create()
		.descriptionId("fluid.create.milk").density(1024).viscosity(1024)
		.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL).sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

	public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MILK = DeferredHolder.create(Registries.FLUID, Create.asResource("milk"));
	public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MILK = DeferredHolder.create(Registries.FLUID, Create.asResource("flowing_milk"));

	private static boolean milkEnabled;

	/** Registers the milk fluid pair (source + flowing, vanilla milk bucket, no block). Idempotent. */
	public static void enableMilkFluid() {
		if (milkEnabled)
			return;
		milkEnabled = true;
		BaseFlowingFluid.Properties props = new BaseFlowingFluid.Properties(MILK_TYPE::value, MILK, FLOWING_MILK)
			.bucket(() -> Items.MILK_BUCKET);
		net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FLUID, MILK.getId(), new BaseFlowingFluid.Source(props));
		net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FLUID, FLOWING_MILK.getId(), new BaseFlowingFluid.Flowing(props));
		FluidTypeRegistry.publishToFabric(MILK.get(), MILK_TYPE.value());
		FluidTypeRegistry.publishToFabric(FLOWING_MILK.get(), MILK_TYPE.value());
	}
}
