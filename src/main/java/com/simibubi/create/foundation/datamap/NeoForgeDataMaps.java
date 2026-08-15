package com.simibubi.create.foundation.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * NeoForge's built-in data maps that Create's data ships (furnace fuels, oxidizables, waxables), bridged
 * into the equivalent Fabric registries via apply hooks so the vendored JSON keeps working.
 */
public final class NeoForgeDataMaps {

	private NeoForgeDataMaps() {}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("neoforge", path);
	}

	public record FurnaceFuel(int burnTime) {
		public static final Codec<FurnaceFuel> CODEC = ExtraCodecs.POSITIVE_INT.fieldOf("burn_time").xmap(FurnaceFuel::new, FurnaceFuel::burnTime).codec();
	}

	public record Oxidizable(Block nextOxidationStage) {
		public static final Codec<Oxidizable> CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("next_oxidation_stage").xmap(Oxidizable::new, Oxidizable::nextOxidationStage).codec();
	}

	public record Waxable(Block waxed) {
		public static final Codec<Waxable> CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("waxed").xmap(Waxable::new, Waxable::waxed).codec();
	}

	public static final DataMapType<Item, FurnaceFuel> FURNACE_FUELS = DataMaps.register(DataMapType
		.builder(id("furnace_fuels"), Registries.ITEM, FurnaceFuel.CODEC)
		.applyHook((item, fuel) -> FuelRegistry.INSTANCE.add(item, fuel.burnTime())).build());

	public static final DataMapType<Block, Oxidizable> OXIDIZABLES = DataMaps.register(DataMapType
		.builder(id("oxidizables"), Registries.BLOCK, Oxidizable.CODEC)
		.applyHook((block, ox) -> OxidizableBlocksRegistry.registerOxidizableBlockPair(block, ox.nextOxidationStage())).build());

	public static final DataMapType<Block, Waxable> WAXABLES = DataMaps.register(DataMapType
		.builder(id("waxables"), Registries.BLOCK, Waxable.CODEC)
		.applyHook((block, wax) -> OxidizableBlocksRegistry.registerWaxableBlockPair(block, wax.waxed())).build());

	/** Forces class init so the types register before data loads. */
	public static void init() {}
}
