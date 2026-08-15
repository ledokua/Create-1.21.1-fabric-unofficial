package com.simibubi.create.foundation.transfer.fluid.crafting;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.level.material.Fluid;

/**
 * Fluid(s) + a data-component predicate (e.g. "potion fluid with contents = night vision"). JSON:
 * {@code {"type": "create:components", "fluids": <id | [ids] | #tag>, "components": {...}, "strict": bool}}.
 */
public class DataComponentFluidIngredient extends FluidIngredient {

	public static final MapCodec<DataComponentFluidIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		HolderSetCodec.create(Registries.FLUID, BuiltInRegistries.FLUID.holderByNameCodec(), false).fieldOf("fluids").forGetter(DataComponentFluidIngredient::fluids),
		DataComponentPredicate.CODEC.fieldOf("components").forGetter(DataComponentFluidIngredient::components),
		Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentFluidIngredient::isStrict))
		.apply(i, DataComponentFluidIngredient::new));
	public static final FluidIngredientType<DataComponentFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

	private final HolderSet<Fluid> fluids;
	private final DataComponentPredicate components;
	private final boolean strict;

	public DataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentPredicate components, boolean strict) {
		this.fluids = fluids;
		this.components = components;
		this.strict = strict;
	}

	@Override
	public boolean test(FluidStack stack) {
		if (!fluids.contains(stack.getFluidHolder()))
			return false;
		if (strict)
			return components.asPatch().equals(stack.getComponentsPatch());
		return components.test(stack);
	}

	@Override
	protected Stream<FluidStack> generateStacks() {
		return fluids.stream().map(h -> new FluidStack(h, FluidStack.BUCKET_VOLUME, components.asPatch()));
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public FluidIngredientType<?> getType() {
		return TYPE;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fluids, components, strict);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DataComponentFluidIngredient o && o.fluids.equals(fluids) && o.components.equals(components) && o.strict == strict;
	}

	public HolderSet<Fluid> fluids() {
		return fluids;
	}

	public DataComponentPredicate components() {
		return components;
	}

	public boolean isStrict() {
		return strict;
	}

	// ---- factories (same set as upstream) ----

	public static FluidIngredient of(boolean strict, FluidStack stack) {
		return of(strict, stack.getComponents(), stack.getFluid());
	}

	public static <T> FluidIngredient of(boolean strict, DataComponentType<? super T> type, T value, Fluid... fluids) {
		return of(strict, DataComponentPredicate.builder().expect(type, value).build(), fluids);
	}

	public static <T> FluidIngredient of(boolean strict, Supplier<? extends DataComponentType<? super T>> type, T value, Fluid... fluids) {
		return of(strict, type.get(), value, fluids);
	}

	public static FluidIngredient of(boolean strict, DataComponentMap map, Fluid... fluids) {
		return of(strict, DataComponentPredicate.allOf(map), fluids);
	}

	@SafeVarargs
	public static FluidIngredient of(boolean strict, DataComponentMap map, Holder<Fluid>... fluids) {
		return of(strict, DataComponentPredicate.allOf(map), fluids);
	}

	public static FluidIngredient of(boolean strict, DataComponentMap map, HolderSet<Fluid> fluids) {
		return of(strict, DataComponentPredicate.allOf(map), fluids);
	}

	@SafeVarargs
	public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Holder<Fluid>... fluids) {
		return of(strict, predicate, HolderSet.direct(fluids));
	}

	public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, Fluid... fluids) {
		return of(strict, predicate, HolderSet.direct(Fluid::builtInRegistryHolder, fluids));
	}

	public static FluidIngredient of(boolean strict, DataComponentPredicate predicate, HolderSet<Fluid> fluids) {
		return new DataComponentFluidIngredient(fluids, predicate, strict);
	}
}
