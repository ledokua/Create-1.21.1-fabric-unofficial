package com.simibubi.create.foundation.transfer.fluid.crafting;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Codec pair identifying a {@link FluidIngredient} subtype; registered by id in {@link FluidIngredient#register}. */
public record FluidIngredientType<T extends FluidIngredient>(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {

	/** Stream codec derived from the map codec (fine for the small payloads ingredients are). */
	public FluidIngredientType(MapCodec<T> codec) {
		this(codec, ByteBufCodecs.fromCodecWithRegistries(codec.codec()));
	}
}
