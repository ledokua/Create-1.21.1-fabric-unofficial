package com.simibubi.create.foundation.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Convention ("c:" namespace) tags Create references, with the constant names upstream uses (from NeoForge's
 * {@code Tags} class). On 1.21 Fabric's {@code ConventionalItemTags} etc. use the very same {@code c:} ids, so
 * this interoperates with the whole Fabric ecosystem — only the constant names differ, which is why this
 * class exists (keeps ported code shape). Ids are data; the tag contents come from vendored/generated JSON.
 * See PORTING_PLAN.md, "NeoForge → Fabric translation strategy".
 */
public final class Tags {

	private Tags() {}

	private static ResourceLocation c(String path) {
		return ResourceLocation.fromNamespaceAndPath("c", path);
	}

	public static final class Blocks {
		private Blocks() {}

		public static final TagKey<Block> GLASS_BLOCKS_COLORLESS = TagKey.create(Registries.BLOCK, c("glass_blocks/colorless"));
		public static final TagKey<Block> GLASS_PANES = TagKey.create(Registries.BLOCK, c("glass_panes"));
		public static final TagKey<Block> GLASS_PANES_COLORLESS = TagKey.create(Registries.BLOCK, c("glass_panes/colorless"));
		public static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, c("ores"));
		public static final TagKey<Block> ORES_IN_GROUND_DEEPSLATE = TagKey.create(Registries.BLOCK, c("ores_in_ground/deepslate"));
		public static final TagKey<Block> ORES_IN_GROUND_STONE = TagKey.create(Registries.BLOCK, c("ores_in_ground/stone"));
		public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = TagKey.create(Registries.BLOCK, c("relocation_not_supported"));
		public static final TagKey<Block> STORAGE_BLOCKS = TagKey.create(Registries.BLOCK, c("storage_blocks"));
	}

	public static final class Fluids {
		private Fluids() {}

		public static final TagKey<Fluid> HONEY = TagKey.create(Registries.FLUID, c("honey"));
		public static final TagKey<Fluid> MILK = TagKey.create(Registries.FLUID, c("milk"));
	}

	public static final class Items {
		private Items() {}

		public static final TagKey<Item> BARRELS_WOODEN = TagKey.create(Registries.ITEM, c("barrels/wooden"));
		public static final TagKey<Item> BUCKETS = TagKey.create(Registries.ITEM, c("buckets"));
		public static final TagKey<Item> CHESTS_WOODEN = TagKey.create(Registries.ITEM, c("chests/wooden"));
		public static final TagKey<Item> COBBLESTONES = TagKey.create(Registries.ITEM, c("cobblestones"));
		public static final TagKey<Item> DUSTS_REDSTONE = TagKey.create(Registries.ITEM, c("dusts/redstone"));
		public static final TagKey<Item> DYES = TagKey.create(Registries.ITEM, c("dyes"));
		public static final TagKey<Item> DYES_BLACK = TagKey.create(Registries.ITEM, c("dyes/black"));
		public static final TagKey<Item> DYES_LIGHT_BLUE = TagKey.create(Registries.ITEM, c("dyes/light_blue"));
		public static final TagKey<Item> DYES_LIME = TagKey.create(Registries.ITEM, c("dyes/lime"));
		public static final TagKey<Item> EGGS = TagKey.create(Registries.ITEM, c("eggs"));
		public static final TagKey<Item> ENCHANTABLES = TagKey.create(Registries.ITEM, c("enchantables"));
		public static final TagKey<Item> ENDER_PEARLS = TagKey.create(Registries.ITEM, c("ender_pearls"));
		public static final TagKey<Item> FEATHERS = TagKey.create(Registries.ITEM, c("feathers"));
		public static final TagKey<Item> FOODS = TagKey.create(Registries.ITEM, c("foods"));
		public static final TagKey<Item> FOODS_DOUGH = TagKey.create(Registries.ITEM, c("foods/dough"));
		public static final TagKey<Item> GEMS_LAPIS = TagKey.create(Registries.ITEM, c("gems/lapis"));
		public static final TagKey<Item> GEMS_QUARTZ = TagKey.create(Registries.ITEM, c("gems/quartz"));
		public static final TagKey<Item> GLASS_BLOCKS = TagKey.create(Registries.ITEM, c("glass_blocks"));
		public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = TagKey.create(Registries.ITEM, c("glass_blocks/colorless"));
		public static final TagKey<Item> GLASS_PANES = TagKey.create(Registries.ITEM, c("glass_panes"));
		public static final TagKey<Item> GLASS_PANES_COLORLESS = TagKey.create(Registries.ITEM, c("glass_panes/colorless"));
		public static final TagKey<Item> GUNPOWDERS = TagKey.create(Registries.ITEM, c("gunpowders"));
		public static final TagKey<Item> INGOTS_COPPER = TagKey.create(Registries.ITEM, c("ingots/copper"));
		public static final TagKey<Item> INGOTS_GOLD = TagKey.create(Registries.ITEM, c("ingots/gold"));
		public static final TagKey<Item> INGOTS_IRON = TagKey.create(Registries.ITEM, c("ingots/iron"));
		public static final TagKey<Item> INGOTS_NETHERITE = TagKey.create(Registries.ITEM, c("ingots/netherite"));
		public static final TagKey<Item> LEATHERS = TagKey.create(Registries.ITEM, c("leathers"));
		public static final TagKey<Item> NETHERRACKS = TagKey.create(Registries.ITEM, c("netherracks"));
		public static final TagKey<Item> NUGGETS = TagKey.create(Registries.ITEM, c("nuggets"));
		public static final TagKey<Item> NUGGETS_IRON = TagKey.create(Registries.ITEM, c("nuggets/iron"));
		public static final TagKey<Item> ORES = TagKey.create(Registries.ITEM, c("ores"));
		public static final TagKey<Item> ORES_IN_GROUND_DEEPSLATE = TagKey.create(Registries.ITEM, c("ores_in_ground/deepslate"));
		public static final TagKey<Item> ORES_IN_GROUND_STONE = TagKey.create(Registries.ITEM, c("ores_in_ground/stone"));
		public static final TagKey<Item> RAW_MATERIALS_COPPER = TagKey.create(Registries.ITEM, c("raw_materials/copper"));
		public static final TagKey<Item> RAW_MATERIALS_GOLD = TagKey.create(Registries.ITEM, c("raw_materials/gold"));
		public static final TagKey<Item> RAW_MATERIALS_IRON = TagKey.create(Registries.ITEM, c("raw_materials/iron"));
		public static final TagKey<Item> RODS_WOODEN = TagKey.create(Registries.ITEM, c("rods/wooden"));
		public static final TagKey<Item> SANDS_COLORLESS = TagKey.create(Registries.ITEM, c("sands/colorless"));
		public static final TagKey<Item> SANDS_RED = TagKey.create(Registries.ITEM, c("sands/red"));
		public static final TagKey<Item> SLIMEBALLS = TagKey.create(Registries.ITEM, c("slimeballs"));
		public static final TagKey<Item> STONES = TagKey.create(Registries.ITEM, c("stones"));
		public static final TagKey<Item> STORAGE_BLOCKS = TagKey.create(Registries.ITEM, c("storage_blocks"));
		public static final TagKey<Item> STORAGE_BLOCKS_COPPER = TagKey.create(Registries.ITEM, c("storage_blocks/copper"));
		public static final TagKey<Item> STORAGE_BLOCKS_IRON = TagKey.create(Registries.ITEM, c("storage_blocks/iron"));
		public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = TagKey.create(Registries.ITEM, c("storage_blocks/raw_copper"));
		public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = TagKey.create(Registries.ITEM, c("storage_blocks/raw_gold"));
		public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = TagKey.create(Registries.ITEM, c("storage_blocks/raw_iron"));
		public static final TagKey<Item> STRINGS = TagKey.create(Registries.ITEM, c("strings"));
		public static final TagKey<Item> STRIPPED_LOGS = TagKey.create(Registries.ITEM, c("stripped_logs"));
		public static final TagKey<Item> STRIPPED_WOODS = TagKey.create(Registries.ITEM, c("stripped_woods"));
		public static final TagKey<Item> TOOLS_WRENCH = TagKey.create(Registries.ITEM, c("tools/wrench"));
	}

}
