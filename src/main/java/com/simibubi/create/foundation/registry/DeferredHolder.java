package com.simibubi.create.foundation.registry;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * A registry-object handle that resolves lazily against the built-in registry (Create-owned; upstream uses
 * NeoForge's class of the same name/shape). Created by {@link DeferredRegister#register}, or by
 * {@link #create} for foreign entries. Also a {@link Holder} so it plugs into vanilla APIs.
 */
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {

	public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation id) {
		return new DeferredHolder<>(ResourceKey.create(registryKey, id));
	}

	public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
		return new DeferredHolder<>(key);
	}

	protected final ResourceKey<R> key;
	@Nullable
	private Holder<R> holder;

	protected DeferredHolder(ResourceKey<R> key) {
		this.key = Objects.requireNonNull(key);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected Registry<R> getRegistry() {
		return (Registry<R>) BuiltInRegistries.REGISTRY.get(key.registry());
	}

	protected final void bind(boolean throwOnMissingRegistry) {
		if (holder != null)
			return;
		Registry<R> registry = getRegistry();
		if (registry != null)
			holder = registry.getHolder(key).orElse(null);
		else if (throwOnMissingRegistry)
			throw new IllegalStateException("Registry not present for " + this + ": " + key.registry());
	}

	@Override
	@SuppressWarnings("unchecked")
	public T get() {
		bind(true);
		if (holder == null)
			throw new NullPointerException("Registry object not present: " + key);
		return (T) holder.value();
	}

	public Optional<T> asOptional() {
		return isBound() ? Optional.of(get()) : Optional.empty();
	}

	public ResourceKey<R> getKey() {
		return key;
	}

	public ResourceLocation getId() {
		return key.location();
	}

	public Holder<R> getDelegate() {
		bind(true);
		return Objects.requireNonNull(holder, "Registry object not present: " + key);
	}

	@Override
	public R value() {
		return get();
	}

	@Override
	public boolean isBound() {
		bind(false);
		return holder != null && holder.isBound();
	}

	@Override
	public boolean is(ResourceLocation location) {
		return location.equals(key.location());
	}

	@Override
	public boolean is(ResourceKey<R> resourceKey) {
		return resourceKey == key;
	}

	@Override
	public boolean is(Predicate<ResourceKey<R>> predicate) {
		return predicate.test(key);
	}

	@Override
	public boolean is(TagKey<R> tagKey) {
		bind(false);
		return holder != null && holder.is(tagKey);
	}

	@Override
	public boolean is(Holder<R> other) {
		bind(false);
		return holder != null && holder.is(other);
	}

	@Override
	public Stream<TagKey<R>> tags() {
		bind(false);
		return holder == null ? Stream.empty() : holder.tags();
	}

	@Override
	public Either<ResourceKey<R>, R> unwrap() {
		return Either.left(key);
	}

	@Override
	public Optional<ResourceKey<R>> unwrapKey() {
		return Optional.of(key);
	}

	@Override
	public Kind kind() {
		return Kind.REFERENCE;
	}

	@Override
	public boolean canSerializeIn(HolderOwner<R> owner) {
		bind(false);
		return holder != null && holder.canSerializeIn(owner);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DeferredHolder<?, ?> other && other.key == key;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public String toString() {
		return "DeferredHolder{" + key + "}";
	}
}
