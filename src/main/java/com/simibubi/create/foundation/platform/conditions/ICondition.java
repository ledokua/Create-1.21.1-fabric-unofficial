package com.simibubi.create.foundation.platform.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * Recipe/data load conditions (Create-owned; NeoForge's shape). Only produced by Create's recipe builders
 * during datagen (not run in the port, Phase 11) — at runtime Fabric's resource-condition system is what
 * evaluates vendored JSON, whose {@code neoforge:conditions} are translated on vendoring. Kept minimal.
 */
public interface ICondition {

	Codec<ICondition> CODEC = Codec.unit(() -> new ICondition() {
		@Override
		public boolean test(IContext context) {
			return true;
		}

		@Override
		public MapCodec<? extends ICondition> codec() {
			return MapCodec.unit(this);
		}
	});

	boolean test(IContext context);

	MapCodec<? extends ICondition> codec();

	interface IContext {
		IContext EMPTY = new IContext() {};

		default <T> boolean isTagLoaded(TagKey<T> key) {
			return true;
		}

		default HolderLookup.Provider registries() {
			return net.minecraft.core.RegistryAccess.EMPTY;
		}
	}

	static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("neoforge", path);
	}
}
