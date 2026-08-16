package com.tterrag.registrate.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockEntityBuilder.BlockEntityFactory;
import com.tterrag.registrate.fabric.RegistrateClientHooks;
import com.tterrag.registrate.fabric.RegistrateEnv;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

public class BlockBuilder<T extends Block, P> extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {

	public static <T extends Block, P> BlockBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name,
		BuilderCallback callback, NonNullFunction<BlockBehaviour.Properties, T> factory) {
		return new BlockBuilder<>(owner, parent, name, callback, factory, BlockBehaviour.Properties::of)
			.defaultBlockstate().defaultLoot().defaultLang();
	}

	private final NonNullFunction<BlockBehaviour.Properties, T> factory;
	private NonNullSupplier<BlockBehaviour.Properties> initialProperties;
	private NonNullFunction<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesCallback = p -> p;
	private final List<Supplier<Supplier<RenderType>>> renderLayers = new ArrayList<>(1);

	protected BlockBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		NonNullFunction<BlockBehaviour.Properties, T> factory, NonNullSupplier<BlockBehaviour.Properties> initialProperties) {
		super(owner, parent, name, callback, Registries.BLOCK);
		this.factory = factory;
		this.initialProperties = initialProperties;
	}

	public BlockBuilder<T, P> properties(NonNullUnaryOperator<BlockBehaviour.Properties> func) {
		propertiesCallback = propertiesCallback.andThen(func)::apply;
		return this;
	}

	public BlockBuilder<T, P> initialProperties(NonNullSupplier<? extends Block> block) {
		this.initialProperties = () -> BlockBehaviour.Properties.ofFullCopy(block.get());
		return this;
	}

	public BlockBuilder<T, P> initialProperties(NonNullFunction<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesFunc) {
		// Explicit-properties variant used by a few Create call sites; overrides the seed entirely.
		this.initialProperties = () -> propertiesFunc.apply(BlockBehaviour.Properties.of());
		return this;
	}

	/** Render layer for this block (client). Registered via Fabric's BlockRenderLayerMap after the block resolves. */
	public BlockBuilder<T, P> addLayer(Supplier<Supplier<RenderType>> layer) {
		if (renderLayers.isEmpty()) {
			onRegister(block -> RegistrateEnv.runOnClient(() -> () ->
				renderLayers.forEach(l -> RegistrateClientHooks.registerRenderLayer(block, l))));
		}
		renderLayers.add(layer);
		return this;
	}

	public BlockBuilder<T, P> simpleItem() {
		return item().build();
	}

	public ItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
		return item(BlockItem::new);
	}

	public <I extends Item> ItemBuilder<I, BlockBuilder<T, P>> item(NonNullBiFunction<? super T, Item.Properties, ? extends I> factory) {
		return getOwner().<I, BlockBuilder<T, P>>item(this, getName(), p -> factory.apply(getEntry(), p));
	}

	public <BE extends BlockEntity> BlockBuilder<T, P> simpleBlockEntity(BlockEntityFactory<BE> factory) {
		return blockEntity(factory).build();
	}

	public <BE extends BlockEntity> BlockEntityBuilder<BE, BlockBuilder<T, P>> blockEntity(BlockEntityFactory<BE> factory) {
		return getOwner().<BE, BlockBuilder<T, P>>blockEntity(this, getName(), factory).validBlock(asSupplier());
	}

	/** Client extension registration (particles etc.); stored in ClientExtensionsManager after the block resolves. */
	public BlockBuilder<T, P> clientExtension(NonNullSupplier<Supplier<com.simibubi.create.foundation.client.extensions.IClientBlockExtensions>> clientExtension) {
		return onRegister(block -> RegistrateEnv.runOnClient(() -> () ->
			com.simibubi.create.foundation.client.extensions.ClientExtensionsManager.register(block, () -> clientExtension.get().get())));
	}

	public BlockBuilder<T, P> clientExtension(java.util.function.Function<T, NonNullSupplier<Supplier<com.simibubi.create.foundation.client.extensions.IClientBlockExtensions>>> clientExtension) {
		return onRegister(block -> RegistrateEnv.runOnClient(() -> () ->
			com.simibubi.create.foundation.client.extensions.ClientExtensionsManager.register(block, () -> clientExtension.apply(block).get().get())));
	}

	/** Block color handler (client). Registered via Fabric's ColorProviderRegistry after the block resolves. */
	public BlockBuilder<T, P> color(NonNullSupplier<Supplier<BlockColor>> colorHandler) {
		return onRegister(block -> RegistrateEnv.runOnClient(() -> () -> RegistrateClientHooks.registerBlockColor(block, colorHandler)));
	}

	// ---- datagen hooks: accepted and dropped for now, see PORTING_PLAN.md Phase 0b/11 ----

	public BlockBuilder<T, P> defaultBlockstate() {
		return this;
	}

	public BlockBuilder<T, P> blockstate(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cons) {
		return setData(ProviderType.BLOCKSTATE, cons);
	}

	public BlockBuilder<T, P> defaultLang() {
		return this;
	}

	@Override
	public BlockBuilder<T, P> lang(String name) {
		return this;
	}

	public BlockBuilder<T, P> defaultLoot() {
		return this;
	}

	public BlockBuilder<T, P> loot(NonNullBiConsumer<RegistrateBlockLootTables, T> cons) {
		return this;
	}

	public BlockBuilder<T, P> recipe(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateRecipeProvider> cons) {
		return setData(ProviderType.RECIPE, cons);
	}

	@SafeVarargs
	public final BlockBuilder<T, P> tag(TagKey<Block>... tags) {
		return tagInternal(tags);
	}

	// ---- construction ----

	@Override
	protected T createEntry() {
		BlockBehaviour.Properties properties = propertiesCallback.apply(initialProperties.get());
		return factory.apply(properties);
	}

	@Override
	protected RegistryEntry<Block, T> createEntryWrapper(ResourceLocation id) {
		return BlockEntry.unresolved(id);
	}

	@Override
	public BlockEntry<T> register() {
		return (BlockEntry<T>) super.register();
	}
}
