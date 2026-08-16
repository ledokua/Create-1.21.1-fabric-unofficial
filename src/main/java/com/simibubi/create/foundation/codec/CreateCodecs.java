package com.simibubi.create.foundation.codec;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredientOld;
import com.simibubi.create.foundation.item.ItemSlots;

import net.minecraft.util.ExtraCodecs;

import com.simibubi.create.foundation.platform.NeoForgeExtraCodecs;
import com.simibubi.create.foundation.transfer.fluid.crafting.FluidIngredient;
import com.simibubi.create.foundation.transfer.fluid.crafting.SizedFluidIngredient;
import com.simibubi.create.foundation.transfer.item.ItemStackHandler;

public class CreateCodecs {
	public static final Codec<Integer> INT_STR = Codec.STRING.comapFlatMap(
		string -> {
			try {
				return DataResult.success(Integer.parseInt(string));
			} catch (NumberFormatException ignored) {
				return DataResult.error(() -> "Not an integer: " + string);
			}
		},
		String::valueOf
	);

	public static final Codec<ItemStackHandler> ITEM_STACK_HANDLER = Codec.lazyInitialized(() -> ItemSlots.CODEC.xmap(
		slots -> slots.toHandler(ItemStackHandler::new), ItemSlots::fromHandler
	));

	public static Codec<Integer> boundedIntStr(int min) {
		return INT_STR.validate(i -> i >= min ? DataResult.success(i) : DataResult.error(() -> "Value under minimum of " + min));
	}

	public static final Codec<Double> NON_NEGATIVE_DOUBLE = doubleRangeWithMessage(0, Double.MAX_VALUE,
		i -> "Value must be non-negative: " + i);
	public static final Codec<Double> POSITIVE_DOUBLE = doubleRangeWithMessage(1, Double.MAX_VALUE,
		i -> "Value must be positive: " + i);

	private static Codec<Double> doubleRangeWithMessage(double min, double max, Function<Double, String> errorMessage) {
		return Codec.DOUBLE.validate(i ->
			i.compareTo(min) >= 0 && i.compareTo(max) <= 0 ? DataResult.success(i) : DataResult.error(() ->
				errorMessage.apply(i)
			)
		);
	}

	public static Codec<SizedFluidIngredient> FLAT_SIZED_FLUID_INGREDIENT_WITH_TYPE = RecordCodecBuilder.create(instance -> instance.group(
		FluidIngredient.TYPE_CODEC.fieldOf("type").forGetter(i -> i.ingredient().getType()),
		FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
		NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", 1000).forGetter(SizedFluidIngredient::amount)
	).apply(instance, (type, ingredient, amount) -> new SizedFluidIngredient(ingredient, amount)));

	/**
	 * Fabric port: the fluid-ingredient JSON of Create Fabric 0.5.x ({@code {"fluid": id | "fluidTag": tag, "amount": droplets}}),
	 * still emitted by other Fabric mods' Create integrations (recipes under {@code <mod>:integration/create/...}).
	 * Amounts are droplets (81000 per bucket) and are converted to mB. Decode-only in spirit; encoding writes the same shape.
	 */
	private record LegacyFluidJson(java.util.Optional<net.minecraft.resources.ResourceLocation> fluid, java.util.Optional<net.minecraft.resources.ResourceLocation> fluidTag, int droplets) {}

	public static Codec<SizedFluidIngredient> LEGACY_FABRIC_SIZED_FLUID_INGREDIENT = RecordCodecBuilder.<LegacyFluidJson>create(instance -> instance.group(
		net.minecraft.resources.ResourceLocation.CODEC.optionalFieldOf("fluid").forGetter(LegacyFluidJson::fluid),
		net.minecraft.resources.ResourceLocation.CODEC.optionalFieldOf("fluidTag").forGetter(LegacyFluidJson::fluidTag),
		ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 81000).forGetter(LegacyFluidJson::droplets)
	).apply(instance, LegacyFluidJson::new)).comapFlatMap(json -> {
		FluidIngredient ingredient;
		if (json.fluid().isPresent()) {
			var holder = net.minecraft.core.registries.BuiltInRegistries.FLUID.getHolder(
				net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.FLUID, json.fluid().get()));
			if (holder.isEmpty())
				return DataResult.error(() -> "Unknown fluid " + json.fluid().get());
			ingredient = FluidIngredient.single(holder.get());
		} else if (json.fluidTag().isPresent()) {
			ingredient = FluidIngredient.tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.FLUID, json.fluidTag().get()));
		} else {
			return DataResult.error(() -> "Legacy fluid ingredient needs 'fluid' or 'fluidTag'");
		}
		return DataResult.success(new SizedFluidIngredient(ingredient, Math.max(1, json.droplets() / 81)));
	}, sized -> new LegacyFluidJson(java.util.Optional.empty(), java.util.Optional.empty(), sized.amount() * 81));

	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public static Codec<SizedFluidIngredient> SIZED_FLUID_INGREDIENT = Codec.withAlternative(
		Codec.withAlternative(FLAT_SIZED_FLUID_INGREDIENT_WITH_TYPE, FluidIngredientOld.CODEC), LEGACY_FABRIC_SIZED_FLUID_INGREDIENT);


}
