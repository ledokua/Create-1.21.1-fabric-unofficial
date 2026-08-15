package com.simibubi.create.foundation.transfer.fluid.crafting;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;

/** A {@link FluidIngredient} plus a required amount (mB). Two JSON layouts, same as upstream: flat and nested. */
public final class SizedFluidIngredient {

	/** {@code {"amount": n, ...ingredient fields}} — what upstream's generated recipes use. */
	public static final Codec<SizedFluidIngredient> FLAT_CODEC = RecordCodecBuilder.create(i -> i.group(
		FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(SizedFluidIngredient::ingredient),
		ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", FluidStack.BUCKET_VOLUME).forGetter(SizedFluidIngredient::amount))
		.apply(i, SizedFluidIngredient::new));

	/** {@code {"ingredient": {...}, "amount": n}}. */
	public static final Codec<SizedFluidIngredient> NESTED_CODEC = RecordCodecBuilder.create(i -> i.group(
		FluidIngredient.CODEC_NON_EMPTY.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
		ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", FluidStack.BUCKET_VOLUME).forGetter(SizedFluidIngredient::amount))
		.apply(i, SizedFluidIngredient::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC = StreamCodec.composite(
		FluidIngredient.STREAM_CODEC, SizedFluidIngredient::ingredient,
		ByteBufCodecs.VAR_INT, SizedFluidIngredient::amount,
		SizedFluidIngredient::new);

	public static SizedFluidIngredient of(Fluid fluid, int amount) {
		return new SizedFluidIngredient(FluidIngredient.of(fluid), amount);
	}

	/** Matches fluid + components of the stack, requiring its amount. */
	public static SizedFluidIngredient of(FluidStack stack) {
		return new SizedFluidIngredient(DataComponentFluidIngredient.of(true, stack), stack.getAmount());
	}

	public static SizedFluidIngredient of(TagKey<Fluid> tag, int amount) {
		return new SizedFluidIngredient(FluidIngredient.tag(tag), amount);
	}

	private final FluidIngredient ingredient;
	private final int amount;
	@Nullable
	private FluidStack[] cachedStacks;

	public SizedFluidIngredient(FluidIngredient ingredient, int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException("Size must be positive");
		this.ingredient = ingredient;
		this.amount = amount;
	}

	public FluidIngredient ingredient() {
		return ingredient;
	}

	public int amount() {
		return amount;
	}

	/** Fluid + components must match AND the stack must hold at least {@link #amount()}. */
	public boolean test(FluidStack stack) {
		return ingredient.test(stack) && stack.getAmount() >= amount;
	}

	/** Representative stacks, sized to {@link #amount()}. */
	public FluidStack[] getFluids() {
		if (cachedStacks == null) {
			FluidStack[] stacks = ingredient.getStacks();
			FluidStack[] sized = new FluidStack[stacks.length];
			for (int i = 0; i < stacks.length; i++)
				sized[i] = stacks[i].copyWithAmount(amount);
			cachedStacks = sized;
		}
		return cachedStacks;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SizedFluidIngredient other && amount == other.amount && ingredient.equals(other.ingredient);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ingredient, amount);
	}

	@Override
	public String toString() {
		return amount + "x " + ingredient;
	}
}
