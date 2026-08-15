package com.simibubi.create.foundation.registry;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge fires this once per registry during registration; the few upstream handlers Create keeps
 * ({@code AllSoundEvents#register}) just need {@link #register} to work. Registration is immediate on Fabric,
 * so {@code register(key, ...)} for any key applies right away regardless of which registry this "is".
 */
public class RegisterEvent extends Event {

	@Nullable
	private final ResourceKey<? extends Registry<?>> registryKey;

	public RegisterEvent(@Nullable ResourceKey<? extends Registry<?>> registryKey) {
		this.registryKey = registryKey;
	}

	@SuppressWarnings("unchecked")
	public <T> void register(ResourceKey<? extends Registry<T>> key, Consumer<RegisterHelper<T>> consumer) {
		Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
		if (registry == null)
			throw new IllegalStateException("Unknown registry " + key.location());
		consumer.accept((id, value) -> Registry.register(registry, id, value));
	}

	public <T> void register(ResourceKey<? extends Registry<T>> key, ResourceLocation id, java.util.function.Supplier<T> value) {
		register(key, helper -> helper.register(id, value.get()));
	}

	@Nullable
	public ResourceKey<? extends Registry<?>> getRegistryKey() {
		return registryKey;
	}

	@Nullable
	public Registry<?> getRegistry() {
		return registryKey == null ? null : BuiltInRegistries.REGISTRY.get(registryKey.location());
	}

	@FunctionalInterface
	public interface RegisterHelper<T> {
		void register(ResourceLocation id, T value);

		default void register(ResourceKey<T> key, T value) {
			register(key.location(), value);
		}
	}
}
