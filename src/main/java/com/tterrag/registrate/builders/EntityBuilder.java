package com.tterrag.registrate.builders;

import java.util.function.Supplier;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.RegistrateClientHooks;
import com.tterrag.registrate.fabric.RegistrateEnv;
import com.tterrag.registrate.providers.loot.RegistrateEntityLootTables;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public class EntityBuilder<T extends Entity, P> extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> {

	public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name,
		BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
		return new EntityBuilder<>(owner, parent, name, callback, factory, classification).defaultLang();
	}

	private final NonNullSupplier<EntityType.Builder<T>> builder;
	private NonNullConsumer<EntityType.Builder<T>> builderCallback = $ -> {};
	private NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer;
	private boolean attributesConfigured;

	protected EntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		EntityType.EntityFactory<T> factory, MobCategory classification) {
		super(owner, parent, name, callback, Registries.ENTITY_TYPE);
		this.builder = () -> EntityType.Builder.of(factory, classification);
	}

	public EntityBuilder<T, P> properties(NonNullConsumer<EntityType.Builder<T>> cons) {
		builderCallback = builderCallback.andThen(cons)::accept;
		return this;
	}

	/** Entity renderer (client). Registered via Fabric's EntityRendererRegistry after the type resolves. */
	public EntityBuilder<T, P> renderer(NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer) {
		if (this.renderer == null) { // first call only
			onRegister(type -> RegistrateEnv.runOnClient(() -> () -> {
				if (this.renderer != null) {
					RegistrateClientHooks.registerEntityRenderer(type, this.renderer);
				}
			}));
		}
		this.renderer = renderer;
		return this;
	}

	/** Living-entity attributes; Fabric's FabricDefaultAttributeRegistry replaces NeoForge's EntityAttributeCreationEvent. */
	@SuppressWarnings("unchecked")
	public EntityBuilder<T, P> attributes(Supplier<AttributeSupplier.Builder> attributes) {
		if (attributesConfigured) {
			throw new IllegalStateException("Cannot configure attributes more than once");
		}
		attributesConfigured = true;
		return onRegister(type -> FabricDefaultAttributeRegistry.register((EntityType<? extends LivingEntity>) type, attributes.get()));
	}

	// ---- datagen hooks: accepted and dropped for now, see PORTING_PLAN.md Phase 0b/11 ----

	public EntityBuilder<T, P> defaultLang() {
		return this;
	}

	@Override
	public EntityBuilder<T, P> lang(String name) {
		return this;
	}

	public EntityBuilder<T, P> loot(NonNullBiConsumer<RegistrateEntityLootTables, EntityType<T>> cons) {
		return this;
	}

	@SafeVarargs
	public final EntityBuilder<T, P> tag(TagKey<EntityType<?>>... tags) {
		return tagInternal(tags);
	}

	// ---- construction ----

	@Override
	protected EntityType<T> createEntry() {
		EntityType.Builder<T> b = builder.get();
		builderCallback.accept(b);
		return b.build(getName());
	}

	@Override
	protected RegistryEntry<EntityType<?>, EntityType<T>> createEntryWrapper(ResourceLocation id) {
		return EntityEntry.unresolved(id);
	}

	@Override
	public EntityEntry<T> register() {
		return (EntityEntry<T>) super.register();
	}
}
