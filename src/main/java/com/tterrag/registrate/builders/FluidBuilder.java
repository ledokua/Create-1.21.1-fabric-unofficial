package com.tterrag.registrate.builders;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Deliberately minimal. Upstream's FluidBuilder (681 lines) is built entirely on NeoForge's
 * {@code FluidType}/{@code BaseFlowingFluid} system, which has no Fabric equivalent — Fabric fluids are done
 * differently (a hand-rolled {@code FlowingFluid} subclass, Transfer API's {@code FluidVariantAttributes},
 * client sprite/render registration via {@code FluidRenderHandlerRegistry}). Create only has 4 fluids
 * (honey, chocolate, and two bucketless "virtual" ones), all created through
 * {@code CreateRegistrate.standardFluid}/{@code virtualFluid}, so the real fluid design happens in Phase 2
 * against those concrete fluids rather than speculatively here. This just gives the registry plumbing a
 * {@code Fluid} entry to flush. See PORTING_PLAN.md.
 */
public class FluidBuilder<T extends Fluid, P> extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {

	public static <T extends Fluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name,
		BuilderCallback callback, NonNullSupplier<T> factory) {
		return new FluidBuilder<>(owner, parent, name, callback, factory);
	}

	private final NonNullSupplier<T> factory;

	protected FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullSupplier<T> factory) {
		super(owner, parent, name, callback, Registries.FLUID);
		this.factory = factory;
	}

	@SafeVarargs
	public final FluidBuilder<T, P> tag(TagKey<Fluid>... tags) {
		return tagInternal(tags);
	}

	@Override
	protected T createEntry() {
		return factory.get();
	}

	@Override
	protected RegistryEntry<Fluid, T> createEntryWrapper(ResourceLocation id) {
		return FluidEntry.unresolved(id);
	}

	@Override
	public FluidEntry<T> register() {
		return (FluidEntry<T>) super.register();
	}
}
