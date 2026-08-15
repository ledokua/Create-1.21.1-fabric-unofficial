package com.tterrag.registrate.builders;

import java.util.function.Supplier;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.RegistrateClientHooks;
import com.tterrag.registrate.fabric.RegistrateEnv;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {

	public static <T extends Item, P> ItemBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name,
		BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
		return new ItemBuilder<>(owner, parent, name, callback, factory).defaultModel().defaultLang();
	}

	private final NonNullFunction<Item.Properties, T> factory;
	private NonNullSupplier<Item.Properties> initialProperties = Item.Properties::new;
	private NonNullFunction<Item.Properties, Item.Properties> propertiesCallback = p -> p;

	protected ItemBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		NonNullFunction<Item.Properties, T> factory) {
		super(owner, parent, name, callback, Registries.ITEM);
		this.factory = factory;
	}

	public ItemBuilder<T, P> properties(NonNullUnaryOperator<Item.Properties> func) {
		propertiesCallback = propertiesCallback.andThen(func)::apply;
		return this;
	}

	public ItemBuilder<T, P> initialProperties(NonNullSupplier<Item.Properties> properties) {
		initialProperties = properties;
		return this;
	}

	/**
	 * Upstream Registrate's own creative-tab DSL. Create doesn't use it (it drives tabs from its own
	 * {@code CreateRegistrate.setCreativeTab} lookup instead — PORTING_PLAN.md Phase 0b), kept as a no-op
	 * for API-surface parity.
	 */
	public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab) {
		return this;
	}

	/** Item color handler (client). Registered via Fabric's ColorProviderRegistry after the item resolves. */
	public ItemBuilder<T, P> color(NonNullSupplier<Supplier<ItemColor>> colorHandler) {
		return onRegister(item -> RegistrateEnv.runOnClient(() -> () -> RegistrateClientHooks.registerItemColor(item, colorHandler)));
	}

	/**
	 * NeoForge Data Map (furnace fuel) — no generic Fabric equivalent. Real support means Fabric's
	 * {@code FuelRegistry}/1.21.1 fuel-values hook at the actual AllItems.java call site (Phase 2), not
	 * through this generic builder hook. Accepted and dropped for now.
	 */
	public ItemBuilder<T, P> burnTime(int tick) {
		return this;
	}

	/** NeoForge Data Map (composting) — same story as {@link #burnTime}; needs ComposterBlock.COMPOSTABLES later. */
	public ItemBuilder<T, P> compostable(float chance) {
		return this;
	}

	// ---- datagen hooks: accepted and dropped for now, see PORTING_PLAN.md Phase 0b/11 ----

	public ItemBuilder<T, P> defaultModel() {
		return this;
	}

	public ItemBuilder<T, P> model(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> cons) {
		return setData(ProviderType.ITEM_MODEL, cons);
	}

	public ItemBuilder<T, P> defaultLang() {
		return this;
	}

	@Override
	public ItemBuilder<T, P> lang(String name) {
		return this;
	}

	public ItemBuilder<T, P> recipe(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateRecipeProvider> cons) {
		return setData(ProviderType.RECIPE, cons);
	}

	@SafeVarargs
	public final ItemBuilder<T, P> tag(TagKey<Item>... tags) {
		return tagInternal(tags);
	}

	// ---- construction ----

	@Override
	protected T createEntry() {
		Item.Properties properties = propertiesCallback.apply(initialProperties.get());
		return factory.apply(properties);
	}

	@Override
	protected RegistryEntry<Item, T> createEntryWrapper(ResourceLocation id) {
		return ItemEntry.unresolved(id);
	}

	@Override
	public ItemEntry<T> register() {
		return (ItemEntry<T>) super.register();
	}
}
