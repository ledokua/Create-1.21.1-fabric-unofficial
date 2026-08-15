package com.tterrag.registrate.providers;

/**
 * Typed key for a datagen provider kind, used as the argument to {@code Builder.setData}/
 * {@code AbstractRegistrate.addDataGenerator}.
 *
 * Phase 0b stub: real datagen doesn't run yet (see PORTING_PLAN.md, Phase 11 — upstream Create's
 * pre-generated JSON under src/generated/resources can be vendored directly in the meantime), so these
 * constants exist only so ported call sites using {@code ProviderType.BLOCK_TAGS} etc. as a lookup key
 * compile. Each is just an identity token, not wired to an actual generator.
 */
public final class ProviderType<T> {

	public static final ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = new ProviderType<>("blockstate");
	public static final ProviderType<RegistrateItemModelProvider> ITEM_MODEL = new ProviderType<>("item_model");
	public static final ProviderType<RegistrateLangProvider> LANG = new ProviderType<>("lang");
	public static final ProviderType<RegistrateRecipeProvider> RECIPE = new ProviderType<>("recipe");
	public static final ProviderType<Object> LOOT = new ProviderType<>("loot");
	public static final ProviderType<RegistrateTagsProvider<?>> BLOCK_TAGS = new ProviderType<>("block_tags");
	public static final ProviderType<RegistrateTagsProvider<?>> ITEM_TAGS = new ProviderType<>("item_tags");
	public static final ProviderType<RegistrateTagsProvider<?>> ENTITY_TAGS = new ProviderType<>("entity_tags");
	public static final ProviderType<RegistrateTagsProvider<?>> FLUID_TAGS = new ProviderType<>("fluid_tags");

	private final String name;

	private ProviderType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ProviderType[" + name + "]";
	}
}
