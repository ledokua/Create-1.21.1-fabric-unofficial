package com.simibubi.create.foundation.block;

import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashBiMap;

import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

@ApiStatus.Internal
public class CopperRegistries {
	private static final Map<Holder<Block>, Holder<Block>> WEATHERING = HashBiMap.create();
	private static final Map<Holder<Block>, Holder<Block>> WAXABLE = HashBiMap.create();

	public static Map<Holder<Block>, Holder<Block>> getWeatheringView() {
		return Collections.unmodifiableMap(WEATHERING);
	}

	public static Map<Holder<Block>, Holder<Block>> getWaxableView() {
		return Collections.unmodifiableMap(WAXABLE);
	}

	public static synchronized void addWeathering(Holder<Block> original, Holder<Block> weathered) {
		WEATHERING.put(original, weathered);
		// Fabric port: also feed Fabric's oxidizable registry (upstream relies on NeoForge data maps for this)
		if (original.isBound() && weathered.isBound())
			net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry.registerOxidizableBlockPair(original.value(), weathered.value());
	}

	public static synchronized void addWaxable(Holder<Block> original, Holder<Block> waxed) {
		WAXABLE.put(original, waxed);
		if (original.isBound() && waxed.isBound())
			net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry.registerWaxableBlockPair(original.value(), waxed.value());
	}
}
