package com.tterrag.registrate.builders;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Hook a finished builder hands its resolved config back to, so the owning {@code AbstractRegistrate} can
 * file it for registration. Threaded through every builder exactly like upstream (it's
 * {@code AbstractRegistrate::accept}); builders never touch the registrate's internals directly.
 * <p>
 * Differs from upstream only in {@code entryFactory}'s input: upstream hands the builder a NeoForge
 * {@code DeferredHolder} to wrap, we hand it the entry's {@code ResourceLocation} (there's no holder to
 * wrap — see {@code RegistryEntry}).
 */
@FunctionalInterface
public interface BuilderCallback {

	<R, T extends R> RegistryEntry<R, T> accept(String name, ResourceKey<? extends Registry<R>> registryKey,
		Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator,
		NonNullFunction<ResourceLocation, ? extends RegistryEntry<R, T>> entryFactory);
}
