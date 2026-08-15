package com.simibubi.create.foundation.platform.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.fabricmc.loader.api.FabricLoader;

public record ModLoadedCondition(String modid) implements ICondition {

	public static final MapCodec<ModLoadedCondition> CODEC = Codec.STRING.fieldOf("modid").xmap(ModLoadedCondition::new, ModLoadedCondition::modid);

	@Override
	public boolean test(IContext context) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
