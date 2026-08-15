package com.tterrag.registrate.builders;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.client.extensions.ClientExtensionsManager;
import com.simibubi.create.foundation.client.extensions.IClientFluidTypeExtensions;
import com.simibubi.create.foundation.transfer.fluid.BaseFlowingFluid;
import com.simibubi.create.foundation.transfer.fluid.FluidType;
import com.simibubi.create.foundation.transfer.fluid.FluidTypeRegistry;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.RegistrateEnv;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;

/**
 * Registers a fluid family the way upstream does: this builder's own entry is the <b>flowing</b> fluid
 * ({@code flowing_<name>}), the source fluid is registered under {@code <name>} when {@link #register()}
 * runs, and optional {@link #block()} ({@code <name>}) / {@link #bucket()} ({@code <name>_bucket}) nested
 * builders. Same ids as upstream, so vendored JSON matches. Built on Create's Fabric fluid layer
 * ({@link BaseFlowingFluid}/{@link FluidType}); the type's client extension is registered on the client and
 * its attributes published to Fabric's fluid registries after registration.
 */
public class FluidBuilder<T extends BaseFlowingFluid, P> extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {

	@FunctionalInterface
	public interface FluidTypeFactory {
		FluidType create(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture);
	}

	public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
		return new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory)
			.defaultLang().defaultSource().defaultBlock().defaultBucket();
	}

	private final String sourceName;
	private final String bucketName;
	private final ResourceLocation stillTexture;
	private final ResourceLocation flowingTexture;
	private final NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory;
	private final NonNullSupplier<FluidType> fluidType;
	@Nullable
	private NonNullSupplier<Supplier<IClientFluidTypeExtensions>> clientExtension;
	@Nullable
	private Boolean defaultSource;
	@Nullable
	private Boolean defaultBlock;
	@Nullable
	private Boolean defaultBucket;
	private NonNullConsumer<FluidType.Properties> typeProperties = $ -> {};
	private NonNullConsumer<BaseFlowingFluid.Properties> fluidProperties;
	@Nullable
	private Supplier<RenderType> layer;
	@Nullable
	private NonNullSupplier<? extends BaseFlowingFluid> source;

	public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture,
		FluidTypeFactory typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
		super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
		this.sourceName = name;
		this.bucketName = name + "_bucket";
		this.stillTexture = stillTexture;
		this.flowingTexture = flowingTexture;
		this.fluidFactory = fluidFactory;
		this.fluidType = NonNullSupplier.lazy(() -> typeFactory.create(makeTypeProperties(), stillTexture, flowingTexture));
		String bucketName = this.bucketName;
		this.fluidProperties = p -> p.bucket(() -> owner.<Item, Item>get(bucketName, Registries.ITEM).get())
			.block(() -> (LiquidBlock) owner.<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block>get(sourceName, Registries.BLOCK).get());
	}

	public FluidBuilder<T, P> properties(NonNullConsumer<FluidType.Properties> cons) {
		typeProperties = typeProperties.andThen(cons)::accept;
		return this;
	}

	public FluidBuilder<T, P> fluidProperties(NonNullConsumer<BaseFlowingFluid.Properties> cons) {
		fluidProperties = fluidProperties.andThen(cons)::accept;
		return this;
	}

	public FluidBuilder<T, P> clientExtension(NonNullSupplier<Supplier<IClientFluidTypeExtensions>> clientExtension) {
		this.clientExtension = clientExtension;
		return this;
	}

	public FluidBuilder<T, P> defaultLang() {
		return this;
	}

	@Override
	public FluidBuilder<T, P> lang(String name) {
		return this;
	}

	/** Render layer for the fluid (client); applied through Fabric's BlockRenderLayerMap after registration. */
	public FluidBuilder<T, P> renderType(Supplier<Supplier<RenderType>> layer) {
		this.layer = layer.get();
		return this;
	}

	public FluidBuilder<T, P> defaultSource() {
		if (defaultSource != null)
			throw new IllegalStateException("Cannot set a default source after a custom source has been created");
		defaultSource = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	public FluidBuilder<T, P> source(NonNullFunction<BaseFlowingFluid.Properties, ? extends BaseFlowingFluid> factory) {
		defaultSource = false;
		source = NonNullSupplier.lazy(() -> factory.apply(makeProperties()));
		return this;
	}

	public FluidBuilder<T, P> defaultBlock() {
		if (defaultBlock != null)
			throw new IllegalStateException("Cannot set a default block after a custom block has been created");
		defaultBlock = true;
		return this;
	}

	public BlockBuilder<LiquidBlock, FluidBuilder<T, P>> block() {
		return block(LiquidBlock::new);
	}

	public <B extends LiquidBlock> BlockBuilder<B, FluidBuilder<T, P>> block(NonNullBiFunction<T, BlockBehaviour.Properties, ? extends B> factory) {
		if (defaultBlock == Boolean.FALSE)
			throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
		defaultBlock = false;
		NonNullSupplier<T> supplier = asSupplier();
		return getOwner().<B, FluidBuilder<T, P>>block(this, sourceName, p -> factory.apply(supplier.get(), p))
			.properties(p -> BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER).noLootTable());
	}

	public FluidBuilder<T, P> noBlock() {
		if (defaultBlock == Boolean.FALSE)
			throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
		defaultBlock = false;
		return this;
	}

	public FluidBuilder<T, P> defaultBucket() {
		if (defaultBucket != null)
			throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
		defaultBucket = true;
		return this;
	}

	public ItemBuilder<BucketItem, FluidBuilder<T, P>> bucket() {
		return bucket(BucketItem::new);
	}

	public <I extends BucketItem> ItemBuilder<I, FluidBuilder<T, P>> bucket(NonNullBiFunction<BaseFlowingFluid, Item.Properties, ? extends I> factory) {
		if (defaultBucket == Boolean.FALSE)
			throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
		defaultBucket = false;
		NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
		if (source == null)
			throw new IllegalStateException("Cannot create a bucket before creating a source block");
		return getOwner().<I, FluidBuilder<T, P>>item(this, bucketName, p -> factory.apply(source.get(), p))
			.properties(p -> p.craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1));
	}

	public FluidBuilder<T, P> noBucket() {
		if (defaultBucket == Boolean.FALSE)
			throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
		defaultBucket = false;
		return this;
	}

	@SafeVarargs
	public final FluidBuilder<T, P> tag(TagKey<Fluid>... tags) {
		return tagInternal(tags);
	}

	private BaseFlowingFluid getSource() {
		NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
		if (source == null)
			throw new NullPointerException("Fluid has no source block: " + sourceName);
		return source.get();
	}

	private BaseFlowingFluid.Properties makeProperties() {
		NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
		BaseFlowingFluid.Properties ret = new BaseFlowingFluid.Properties(fluidType, source == null ? null : source::get, asSupplier());
		fluidProperties.accept(ret);
		return ret;
	}

	private FluidType.Properties makeTypeProperties() {
		FluidType.Properties properties = FluidType.Properties.create();
		typeProperties.accept(properties);
		properties.descriptionId(Util.makeDescriptionId("fluid", ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), sourceName)));
		return properties;
	}

	@Override
	protected T createEntry() {
		return fluidFactory.apply(makeProperties());
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FluidEntry<T> register() {
		if (defaultSource == Boolean.TRUE)
			source(BaseFlowingFluid.Source::new);
		if (defaultBlock == Boolean.TRUE)
			block().register();
		if (defaultBucket == Boolean.TRUE)
			bucket().register();

		NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
		if (source == null)
			throw new IllegalStateException("Fluid must have a source version: " + getName());
		getOwner().addRegisterCallback(sourceName, Registries.FLUID, (NonNullConsumer<Fluid>) this::afterRegister);
		getCallback().accept(sourceName, Registries.FLUID, (Builder) this, source::get, id -> FluidEntry.unresolved(id));

		onRegister(this::afterRegister);
		return (FluidEntry<T>) super.register();
	}

	/** Runs for both the flowing and source fluid: publish the type to Fabric, set up client rendering. */
	private void afterRegister(Fluid fluid) {
		FluidType type = fluidType.get();
		FluidTypeRegistry.publishToFabric(fluid, type);
		RegistrateEnv.runOnClient(() -> () -> {
			IClientFluidTypeExtensions ext = clientExtensionOrFromType(type);
			ClientExtensionsManager.register(fluid, ext);
			com.tterrag.registrate.fabric.RegistrateClientHooks.registerFluidRendering(fluid, ext, layer);
		});
	}

	private IClientFluidTypeExtensions clientExtensionOrFromType(FluidType type) {
		if (clientExtension != null)
			return clientExtension.get().get();
		IClientFluidTypeExtensions[] out = { null };
		type.initializeClient(ext -> out[0] = ext);
		if (out[0] != null)
			return out[0];
		ResourceLocation still = stillTexture, flowing = flowingTexture;
		return new IClientFluidTypeExtensions() {
			@Override
			public ResourceLocation getStillTexture() {
				return still;
			}

			@Override
			public ResourceLocation getFlowingTexture() {
				return flowing;
			}
		};
	}

	@Override
	protected RegistryEntry<Fluid, T> createEntryWrapper(ResourceLocation id) {
		return FluidEntry.unresolved(id);
	}

	protected BuilderCallback getCallback() {
		return callback;
	}
}
