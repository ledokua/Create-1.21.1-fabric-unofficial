package com.simibubi.create.infrastructure.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;

/**
 * Upstream: NeoForge biome-modifier JSON (datagen'd) adding Create's ore features to overworld/nether biomes.
 * Fabric port: the same three additions through Fabric's BiomeModifications API at init.
 */
public class AllBiomeModifiers {

	public static void register() {
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD), Decoration.UNDERGROUND_ORES, AllPlacedFeatures.ZINC_ORE);
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD), Decoration.UNDERGROUND_ORES, AllPlacedFeatures.STRIATED_ORES_OVERWORLD);
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_NETHER), Decoration.UNDERGROUND_ORES, AllPlacedFeatures.STRIATED_ORES_NETHER);
	}
}
