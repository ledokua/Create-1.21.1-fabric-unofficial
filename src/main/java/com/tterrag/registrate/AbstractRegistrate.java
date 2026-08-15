package com.tterrag.registrate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

import com.simibubi.create.foundation.transfer.fluid.BaseFlowingFluid;
import com.simibubi.create.foundation.transfer.fluid.FluidType;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.builders.MenuBuilder.ForgeMenuFactory;
import com.tterrag.registrate.builders.MenuBuilder.MenuFactory;
import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Fabric-native reimplementation of {@code com.tterrag.registrate.AbstractRegistrate}. Same package/class
 * name and public surface as upstream so ported Create code needs no import changes; internals are a
 * from-scratch rewrite — see PORTING_PLAN.md, Phase 0b, for why (upstream is bound end-to-end to NeoForge's
 * {@code DeferredHolder}/{@code RegisterEvent}).
 * <p>
 * Registration is two-phase like upstream (so ported call-order-dependent code keeps working — e.g. a
 * {@code BlockBuilder.item()}'s factory can reference the block's entry before the block has "registered"),
 * but driven by one explicit ordered flush ({@link #register()}) instead of NeoForge firing an event once
 * per registry. Builders file a {@link Registration} when {@code .register()}/{@code .build()} is called;
 * nothing is actually constructed until the flush reaches that registry type.
 */
public abstract class AbstractRegistrate<S extends AbstractRegistrate<S>> {

	private static final Logger LOGGER = LoggerFactory.getLogger("Registrate");

	/**
	 * Registries flushed in this fixed order, mirroring the order NeoForge fires RegisterEvent in (which
	 * upstream Registrate implicitly relies on: block items reference their block, block entity types
	 * reference their valid blocks, etc.). Anything not listed here — e.g. Create's own custom simple
	 * registries — flushes afterwards, in first-touched order.
	 */
	private static final List<ResourceKey<? extends Registry<?>>> FLUSH_ORDER = List.of(
		Registries.SOUND_EVENT, Registries.FLUID, Registries.BLOCK, Registries.ATTRIBUTE, Registries.ENTITY_TYPE,
		Registries.ITEM, Registries.BLOCK_ENTITY_TYPE, Registries.MENU, Registries.CREATIVE_MODE_TAB);

	public static boolean isDevEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	private class Registration<R, T extends R> {
		final ResourceLocation id;
		final ResourceKey<? extends Registry<R>> registryKey;
		final NonNullSupplier<? extends T> creator;
		final RegistryEntry<R, T> entry;
		final List<NonNullConsumer<? super T>> callbacks = new ArrayList<>();

		Registration(ResourceLocation id, ResourceKey<? extends Registry<R>> registryKey, NonNullSupplier<? extends T> creator, RegistryEntry<R, T> entry) {
			this.id = id;
			this.registryKey = registryKey;
			this.creator = creator;
			this.entry = entry;
		}

		@SuppressWarnings("unchecked")
		void flush() {
			Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(registryKey.location());
			if (registry == null) {
				throw new IllegalStateException("Unknown registry " + registryKey.location() + " for entry " + id);
			}
			T value = creator.get();
			Registry.register(registry, id, value);
			entry.resolve(value);
			callbacks.forEach(c -> c.accept(value));
			callbacks.clear();
		}
	}

	private record CallbackKey(String name, ResourceKey<? extends Registry<?>> registryKey) {}

	private final Map<ResourceKey<? extends Registry<?>>, Map<String, Registration<?, ?>>> registrations = new LinkedHashMap<>();
	/** onRegister callbacks added before their entry was filed; drained into the Registration in accept(). */
	private final Map<CallbackKey, List<NonNullConsumer<?>>> registerCallbacks = new LinkedHashMap<>();
	/** Entry-less callbacks run once a whole registry type has flushed (see Builder#onRegisterAfter). */
	private final Map<ResourceKey<? extends Registry<?>>, List<Runnable>> afterRegisterCallbacks = new LinkedHashMap<>();
	private final Set<ResourceKey<? extends Registry<?>>> completedRegistrations = new HashSet<>();
	private final Map<ResourceKey<? extends Registry<?>>, List<RegistryEntry<?, ?>>> allEntries = new LinkedHashMap<>();

	private final String modid;
	private String currentName;
	private boolean flushed;
	private boolean skipErrors;

	protected AbstractRegistrate(String modid) {
		this.modid = modid;
	}

	public String getModid() {
		return modid;
	}

	@SuppressWarnings("unchecked")
	protected final S self() {
		return (S) this;
	}

	/** Dev-only: log registration failures instead of throwing, matching upstream's shape. */
	public S skipErrors(boolean skipErrors) {
		if (skipErrors && !isDevEnvironment()) {
			LOGGER.error("Ignoring skipErrors(true) as this is not a development environment!");
		} else {
			this.skipErrors = skipErrors;
		}
		return self();
	}

	public S object(String name) {
		this.currentName = name;
		return self();
	}

	protected String currentName() {
		if (currentName == null) {
			throw new IllegalStateException("Current name not set (call .object(name) first, or pass a name explicitly)");
		}
		return currentName;
	}

	// ==== Registration bookkeeping ==========================================================

	/**
	 * Hands a fresh {@link BuilderCallback} (bound to {@link #accept}) to a builder factory. This is the seam
	 * every entry-point method goes through, and what Create's own subclasses use to plug in custom builders
	 * ({@code CreateBlockEntityBuilder}, {@code SimpleBuilder}, ...) — same name/shape as upstream.
	 */
	protected <R, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 entry(String name, NonNullFunction<BuilderCallback, S2> factory) {
		return factory.apply(this::accept);
	}

	/**
	 * Files a builder's finished config for registration. Nothing is constructed yet — see class javadoc.
	 * Overridable (Create's {@code CreateRegistrate} hooks it for tooltip modifiers/creative tabs) —
	 * overrides must call super.
	 */
	protected <R, T extends R> RegistryEntry<R, T> accept(String name, ResourceKey<? extends Registry<R>> registryKey,
		Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
		NonNullFunction<ResourceLocation, ? extends RegistryEntry<R, T>> entryFactory) {
		if (flushed) {
			throw new IllegalStateException("Cannot register '" + name + "' after registration has already run for '" + modid + "'");
		}
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
		RegistryEntry<R, T> entry = entryFactory.apply(id);
		Registration<R, T> registration = new Registration<>(id, registryKey, creator, entry);
		List<NonNullConsumer<?>> early = registerCallbacks.remove(new CallbackKey(name, registryKey));
		if (early != null) {
			for (NonNullConsumer<?> c : early) {
				@SuppressWarnings("unchecked")
				NonNullConsumer<? super T> cast = (NonNullConsumer<? super T>) c;
				registration.callbacks.add(cast);
			}
		}
		Registration<?, ?> previous = registrations.computeIfAbsent(registryKey, k -> new LinkedHashMap<>()).put(name, registration);
		if (previous != null) {
			throw new IllegalArgumentException("Duplicate registration '" + name + "' in registry " + registryKey.location());
		}
		allEntries.computeIfAbsent(registryKey, k -> new ArrayList<>()).add(entry);
		return entry;
	}

	/**
	 * Runs the deferred flush: constructs and registers every filed entry, in {@link #FLUSH_ORDER} (then
	 * whatever's left, in first-touched order). Call exactly once from the mod's initializer, after every
	 * {@code AllXxx.register()} static-init has run.
	 */
	public void register() {
		if (flushed) {
			throw new IllegalStateException("Registration has already run for '" + modid + "'");
		}
		Set<ResourceKey<? extends Registry<?>>> ordered = new LinkedHashSet<>(FLUSH_ORDER);
		ordered.addAll(registrations.keySet());
		for (ResourceKey<? extends Registry<?>> key : ordered) {
			flushType(key);
		}
		flushed = true;
		if (!registerCallbacks.isEmpty()) {
			registerCallbacks.forEach((k, v) -> LOGGER.warn("({}) Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?",
				modid, v.size(), k.name(), k.registryKey().location()));
			if (isDevEnvironment()) {
				throw new IllegalStateException("Found unused register callbacks, see logs");
			}
		}
	}

	private void flushType(ResourceKey<? extends Registry<?>> key) {
		Map<String, Registration<?, ?>> forType = registrations.remove(key);
		if (forType != null) {
			LOGGER.debug("({}) Registering {} object(s) of type {}", modid, forType.size(), key.location());
			for (Registration<?, ?> registration : forType.values()) {
				try {
					registration.flush();
				} catch (Exception e) {
					String err = "Unexpected error while registering entry " + registration.id + " to registry " + key.location();
					if (skipErrors) {
						LOGGER.error(err, e);
					} else {
						throw new RuntimeException(err, e);
					}
				}
			}
		}
		completedRegistrations.add(key);
		List<Runnable> after = afterRegisterCallbacks.remove(key);
		if (after != null) {
			after.forEach(Runnable::run);
		}
	}

	public <R, T extends R> void addRegisterCallback(String name, ResourceKey<? extends Registry<R>> registryKey, NonNullConsumer<? super T> callback) {
		Map<String, Registration<?, ?>> forType = registrations.get(registryKey);
		if (forType != null && forType.containsKey(name)) {
			@SuppressWarnings("unchecked")
			Registration<R, T> reg = (Registration<R, T>) forType.get(name);
			reg.callbacks.add(callback);
			return;
		}
		if (completedRegistrations.contains(registryKey)) {
			// already flushed: entry exists (or never will) — fire now if we have it, mirroring upstream's
			// "callbacks fire right after registration" contract for late callers.
			RegistryEntry<R, T> entry = findEntry(name, registryKey);
			if (entry != null && entry.isPresent()) {
				callback.accept(entry.get());
				return;
			}
		}
		registerCallbacks.computeIfAbsent(new CallbackKey(name, registryKey), k -> new ArrayList<>()).add(callback);
	}

	public <R> void addRegisterCallback(ResourceKey<? extends Registry<R>> registryKey, Runnable callback) {
		if (completedRegistrations.contains(registryKey)) {
			callback.run();
			return;
		}
		afterRegisterCallbacks.computeIfAbsent(registryKey, k -> new ArrayList<>()).add(callback);
	}

	public <R> boolean isRegistered(ResourceKey<? extends Registry<R>> registryKey) {
		return completedRegistrations.contains(registryKey);
	}

	public <R, T extends R> RegistryEntry<R, T> get(String name, ResourceKey<? extends Registry<R>> registryKey) {
		RegistryEntry<R, T> entry = findEntry(name, registryKey);
		if (entry == null) {
			throw new NoSuchElementException("No entry '" + name + "' filed for registry " + registryKey.location());
		}
		return entry;
	}

	@SuppressWarnings("unchecked")
	private <R, T extends R> RegistryEntry<R, T> findEntry(String name, ResourceKey<? extends Registry<R>> registryKey) {
		List<RegistryEntry<?, ?>> entries = allEntries.get(registryKey);
		if (entries == null) {
			return null;
		}
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
		for (RegistryEntry<?, ?> entry : entries) {
			if (entry.getId().equals(id)) {
				return (RegistryEntry<R, T>) entry;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <R> Collection<RegistryEntry<R, R>> getAll(ResourceKey<? extends Registry<R>> registryKey) {
		return (Collection<RegistryEntry<R, R>>) (Collection<?>) allEntries.getOrDefault(registryKey, List.of());
	}

	// ==== Datagen / creative-tab hooks (stubs, see PORTING_PLAN.md Phase 0b) ================

	/** Bulk datagen callback. Accepted and dropped for now — real datagen is Phase 11. */
	public <T> S addDataGenerator(ProviderType<? extends T> type, NonNullConsumer<? extends T> cons) {
		return self();
	}

	public S addLang(String type, ResourceLocation id, String localizedName) {
		return self();
	}

	public S addRawLang(String key, String value) {
		return self();
	}

	/**
	 * Upstream Registrate's own creative-tab auto-assignment. Create explicitly disables it
	 * ({@code .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)}) and uses its own lookup table
	 * instead, so this only needs to accept the call. No-op.
	 */
	public S defaultCreativeTab(ResourceKey<CreativeModeTab> tab) {
		return self();
	}

	// ==== Entry-point builder factories =====================================================
	// Each comes in the same overload set as upstream: (factory) using the current .object() name, (name,
	// factory), (parent, factory), (parent, name, factory).

	public <T extends Block> BlockBuilder<T, S> block(NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return block(self(), factory);
	}

	public <T extends Block> BlockBuilder<T, S> block(String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return block(self(), name, factory);
	}

	public <T extends Block, P> BlockBuilder<T, P> block(P parent, NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return block(parent, currentName(), factory);
	}

	public <T extends Block, P> BlockBuilder<T, P> block(P parent, String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return entry(name, callback -> BlockBuilder.create(this, parent, name, callback, factory));
	}

	public <T extends Item> ItemBuilder<T, S> item(NonNullFunction<Item.Properties, T> factory) {
		return item(self(), factory);
	}

	public <T extends Item> ItemBuilder<T, S> item(String name, NonNullFunction<Item.Properties, T> factory) {
		return item(self(), name, factory);
	}

	public <T extends Item, P> ItemBuilder<T, P> item(P parent, NonNullFunction<Item.Properties, T> factory) {
		return item(parent, currentName(), factory);
	}

	public <T extends Item, P> ItemBuilder<T, P> item(P parent, String name, NonNullFunction<Item.Properties, T> factory) {
		return entry(name, callback -> ItemBuilder.create(this, parent, name, callback, factory));
	}

	public <T extends BlockEntity> BlockEntityBuilder<T, S> blockEntity(BlockEntityFactory<T> factory) {
		return blockEntity(self(), factory);
	}

	public <T extends BlockEntity> BlockEntityBuilder<T, S> blockEntity(String name, BlockEntityFactory<T> factory) {
		return blockEntity(self(), name, factory);
	}

	public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(P parent, BlockEntityFactory<T> factory) {
		return blockEntity(parent, currentName(), factory);
	}

	public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(P parent, String name, BlockEntityFactory<T> factory) {
		return entry(name, callback -> BlockEntityBuilder.create(this, parent, name, callback, factory));
	}

	public <T extends Entity> EntityBuilder<T, S> entity(EntityType.EntityFactory<T> factory, MobCategory classification) {
		return entity(self(), factory, classification);
	}

	public <T extends Entity> EntityBuilder<T, S> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return entity(self(), name, factory, classification);
	}

	public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return entity(parent, currentName(), factory, classification);
	}

	public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return entry(name, callback -> EntityBuilder.create(this, parent, name, callback, factory, classification));
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(MenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return menu(self(), factory, screenFactory);
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(String name, MenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return menu(self(), name, factory, screenFactory);
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, MenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return menu(parent, currentName(), factory, screenFactory);
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, String name, MenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return entry(name, callback -> MenuBuilder.create(this, parent, name, callback, factory, screenFactory));
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(ForgeMenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return menu(self(), factory, screenFactory);
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(String name, ForgeMenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return menu(self(), name, factory, screenFactory);
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, ForgeMenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return menu(parent, currentName(), factory, screenFactory);
	}

	public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, String name, ForgeMenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		return entry(name, callback -> MenuBuilder.create(this, parent, name, callback, factory, screenFactory));
	}

	// ---- fluids (subset of upstream's overloads that Create's CreateRegistrate calls) ----

	public FluidBuilder<BaseFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
		return fluid(self(), name, stillTexture, flowingTexture);
	}

	public FluidBuilder<BaseFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory) {
		return fluid(self(), name, stillTexture, flowingTexture, typeFactory);
	}

	public <T extends BaseFlowingFluid> FluidBuilder<T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
		FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
		return fluid(self(), name, stillTexture, flowingTexture, typeFactory, fluidFactory);
	}

	public <P> FluidBuilder<BaseFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
		return this.<BaseFlowingFluid.Flowing, P>fluid(parent, name, stillTexture, flowingTexture, (p, s, f) -> new FluidType(p), BaseFlowingFluid.Flowing::new);
	}

	public <P> FluidBuilder<BaseFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory) {
		return this.<BaseFlowingFluid.Flowing, P>fluid(parent, name, stillTexture, flowingTexture, typeFactory, BaseFlowingFluid.Flowing::new);
	}

	public <T extends BaseFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,
		FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
		return entry(name, callback -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory));
	}

	/** Arbitrary registry entry with no builder-specific config (creative tabs, custom registries, ...). */
	public <R, T extends R> NoConfigBuilder<R, T, S> simple(String name, ResourceKey<Registry<R>> registryKey, NonNullSupplier<T> factory) {
		return simple(self(), name, registryKey, factory);
	}

	public <R, T extends R, P> NoConfigBuilder<R, T, P> simple(P parent, String name, ResourceKey<Registry<R>> registryKey, NonNullSupplier<T> factory) {
		return entry(name, callback -> new NoConfigBuilder<>(this, parent, name, callback, registryKey, factory));
	}

	public <R, T extends R, P> NoConfigBuilder<R, T, P> generic(P parent, String name, ResourceKey<Registry<R>> registryKey, NonNullSupplier<T> factory) {
		return simple(parent, name, registryKey, factory);
	}
}
