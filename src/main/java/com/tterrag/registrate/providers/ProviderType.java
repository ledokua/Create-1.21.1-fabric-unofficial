package com.tterrag.registrate.providers;

import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;

/**
 * Typed key for a datagen provider kind, argument of {@code Builder.setData}/{@code
 * AbstractRegistrate.addDataGenerator}. Phase 0b/11 shell: identity tokens only, not wired to generators —
 * upstream's pre-generated JSON is vendored instead. See PORTING_PLAN.md.
 */
public final class ProviderType<T> {

	public static final ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = new ProviderType<>("blockstate");
	public static final ProviderType<RegistrateItemModelProvider> ITEM_MODEL = new ProviderType<>("item_model");
	public static final ProviderType<RegistrateLangProvider> LANG = new ProviderType<>("lang");
	public static final ProviderType<RegistrateRecipeProvider> RECIPE = new ProviderType<>("recipe");
	public static final ProviderType<RegistrateBlockLootTables> LOOT = new ProviderType<>("loot");
	public static final ProviderType<RegistrateTagsProvider<net.minecraft.world.level.block.Block>> BLOCK_TAGS = new ProviderType<>("block_tags");
	public static final ProviderType<RegistrateTagsProvider<net.minecraft.world.item.Item>> ITEM_TAGS = new ProviderType<>("item_tags");
	public static final ProviderType<RegistrateTagsProvider<net.minecraft.world.entity.EntityType<?>>> ENTITY_TAGS = new ProviderType<>("entity_tags");
	public static final ProviderType<RegistrateTagsProvider<net.minecraft.world.level.material.Fluid>> FLUID_TAGS = new ProviderType<>("fluid_tags");
	public static final ProviderType<Object> ADVANCEMENT = new ProviderType<>("advancement");
	public static final ProviderType<Object> DATA_MAP = new ProviderType<>("data_map");

	private final String name;

	private ProviderType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ProviderType[" + name + "]";
	}
}
