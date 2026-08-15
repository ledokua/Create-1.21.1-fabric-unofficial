package com.simibubi.create.foundation.transfer.fluid.crafting;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** JSON: {@code {"tag": "ns:id"}} (optionally with {@code "type": "create:tag"}). */
public class TagFluidIngredient extends FluidIngredient {

	public static final MapCodec<TagFluidIngredient> CODEC = TagKey.codec(Registries.FLUID)
		.xmap(TagFluidIngredient::new, TagFluidIngredient::tag).fieldOf("tag");
	public static final FluidIngredientType<TagFluidIngredient> TYPE = new FluidIngredientType<>(CODEC);

	private final TagKey<Fluid> tag;

	public TagFluidIngredient(TagKey<Fluid> tag) {
		this.tag = tag;
	}

	@Override
	public boolean test(FluidStack fluidStack) {
		return fluidStack.is(tag);
	}

	@Override
	protected Stream<FluidStack> generateStacks() {
		return StreamSupport.stream(BuiltInRegistries.FLUID.getTagOrEmpty(tag).spliterator(), false)
			.map(holder -> new FluidStack(holder, FluidStack.BUCKET_VOLUME));
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public FluidIngredientType<?> getType() {
		return TYPE;
	}

	@Override
	public int hashCode() {
		return tag.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TagFluidIngredient other && other.tag.equals(tag);
	}

	public TagKey<Fluid> tag() {
		return tag;
	}
}
