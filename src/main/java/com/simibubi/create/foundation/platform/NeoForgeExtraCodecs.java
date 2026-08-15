package com.simibubi.create.foundation.platform;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

/** The one NeoForge codec helper Create uses. */
public final class NeoForgeExtraCodecs {

	private NeoForgeExtraCodecs() {}

	/** Optional field with a default that is always written (unlike {@code optionalFieldOf(name, default)}). */
	public static <T> MapCodec<T> optionalFieldAlwaysWrite(Codec<T> codec, String name, T defaultValue) {
		return codec.optionalFieldOf(name).xmap(o -> o.orElse(defaultValue), Optional::of);
	}
}
