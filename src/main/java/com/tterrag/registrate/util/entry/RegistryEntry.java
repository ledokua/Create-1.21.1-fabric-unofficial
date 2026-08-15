package com.tterrag.registrate.util.entry;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Fabric-native reimplementation of Registrate's public-facing registration handle. Same two-type-parameter
 * shape as upstream 1.21.x ({@code RegistryEntry<R, T>} — registry base type, concrete type), because
 * Create's code spells it out that way at ~50 sites.
 * <p>
 * Upstream extends NeoForge's {@code DeferredHolder}, resolved once NeoForge fires {@code RegisterEvent}.
 * We have no such event, but we still need the same call-order independence it buys upstream (e.g. a
 * BlockBuilder's nested {@code .item()} config can reference the block's entry before the block has
 * "registered" — construction only actually happens at {@code AbstractRegistrate}'s ordered flush). So this
 * is created unresolved and filled in later by that flush via {@link #resolve}, same two-phase shape as
 * upstream, just driven by an explicit ordered pass instead of an event bus. See {@code AbstractRegistrate}.
 */
public class RegistryEntry<R, T extends R> implements NonNullSupplier<T> {

	public static <R, T extends R> RegistryEntry<R, T> unresolved(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation id) {
		return new RegistryEntry<>(registryKey, id);
	}

	private final ResourceKey<? extends Registry<R>> registryKey;
	private final ResourceKey<R> key;
	private T value;

	protected RegistryEntry(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation id) {
		this.registryKey = registryKey;
		this.key = ResourceKey.create(registryKey, id);
	}

	/** Called once by {@code AbstractRegistrate}'s registration flush. Not for general use. */
	public void resolve(T value) {
		if (this.value != null) {
			throw new IllegalStateException("Registry entry " + getId() + " resolved twice");
		}
		this.value = value;
	}

	public ResourceKey<? extends Registry<R>> getRegistryKey() {
		return registryKey;
	}

	/** The entry's own {@code ResourceKey} (registry + id), like {@code DeferredHolder#getKey()} upstream. */
	public ResourceKey<R> getKey() {
		return key;
	}

	public ResourceLocation getId() {
		return key.location();
	}

	@Override
	public T get() {
		if (value == null) {
			throw new NoSuchElementException("Registry entry " + getId() + " has not been registered yet");
		}
		return value;
	}

	public boolean isPresent() {
		return value != null;
	}

	public <X> X map(NonNullFunction<? super T, ? extends X> mapper) {
		return mapper.apply(get());
	}

	public boolean filter(Predicate<? super T> predicate) {
		return isPresent() && predicate.test(value);
	}

	public boolean is(T value) {
		return this.value == value;
	}

	public boolean is(ResourceLocation id) {
		return getId().equals(id);
	}

	public boolean is(ResourceKey<R> key) {
		return this.key.equals(key);
	}

	public NonNullSupplier<T> asSupplier() {
		return this;
	}

	/** Live {@code Holder} from the built-in registry; valid after registration. */
	@SuppressWarnings("unchecked")
	public Holder<R> asHolder() {
		Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.get(registryKey.location());
		if (registry == null) {
			throw new IllegalStateException("Unknown registry " + registryKey.location());
		}
		return registry.getHolderOrThrow(key);
	}

	@Override
	public String toString() {
		return "RegistryEntry[" + getId() + "]";
	}
}
