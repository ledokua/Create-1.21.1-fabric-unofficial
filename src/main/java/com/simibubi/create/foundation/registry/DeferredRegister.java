package com.simibubi.create.foundation.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.foundation.event.bus.IEventBus;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Collects registry entries and registers them all at once (Create-owned; upstream uses NeoForge's class of
 * the same name/shape). NeoForge defers the actual registration to RegisterEvent; Fabric registries are open
 * during init, so {@link #register(IEventBus)} — called from the same {@code AllXxx.register(bus)} sites —
 * registers immediately, in declaration order. Entries added afterwards are registered on the spot.
 */
public class DeferredRegister<T> {

	public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid) {
		return new DeferredRegister<>(registryKey, modid);
	}

	public static <T> DeferredRegister<T> create(Registry<T> registry, String modid) {
		return new DeferredRegister<>(registry.key(), modid);
	}

	public static <T> DeferredRegister<T> create(ResourceLocation registryName, String modid) {
		return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), modid);
	}

	public static DataComponents createDataComponents(String modid) {
		return new DataComponents(modid);
	}

	public static DataComponents createDataComponents(ResourceKey<Registry<DataComponentType<?>>> registryKey, String modid) {
		return new DataComponents(modid);
	}

	private final ResourceKey<? extends Registry<T>> registryKey;
	private final String modid;
	private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
	private boolean registered;

	protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modid) {
		this.registryKey = registryKey;
		this.modid = modid;
	}

	@SuppressWarnings("unchecked")
	public <I extends T> DeferredHolder<T, I> register(String name, Function<ResourceLocation, ? extends I> factory) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
		DeferredHolder<T, I> holder = createHolder((ResourceKey<? extends Registry<T>>) registryKey, id);
		Supplier<I> supplier = () -> factory.apply(id);
		if (registered)
			doRegister(id, supplier);
		else
			entries.put(holder, supplier);
		return holder;
	}

	public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier) {
		return register(name, id -> supplier.get());
	}

	protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id) {
		return DeferredHolder.create(registryKey, id);
	}

	/** Registers everything filed so far. The bus parameter exists for upstream call-site parity. */
	public void register(IEventBus bus) {
		if (registered)
			throw new IllegalStateException("Cannot register DeferredRegister " + registryKey.location() + " twice");
		registered = true;
		entries.forEach((holder, supplier) -> doRegister(holder.getId(), supplier));
		entries.clear();
	}

	@SuppressWarnings("unchecked")
	private void doRegister(ResourceLocation id, Supplier<? extends T> supplier) {
		Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
		if (registry == null)
			throw new IllegalStateException("Registry " + registryKey.location() + " does not exist (needed for " + id + ")");
		Registry.register(registry, id, supplier.get());
	}

	public Collection<DeferredHolder<T, ? extends T>> getEntries() {
		return entries.keySet();
	}

	public ResourceKey<? extends Registry<T>> getRegistryKey() {
		return registryKey;
	}

	public ResourceLocation getRegistryName() {
		return registryKey.location();
	}

	public String getNamespace() {
		return modid;
	}

	/** Specialisation with NeoForge's convenience factory for data component types. */
	public static class DataComponents extends DeferredRegister<DataComponentType<?>> {
		protected DataComponents(String modid) {
			super(Registries.DATA_COMPONENT_TYPE, modid);
		}

		public <D> DeferredHolder<DataComponentType<?>, DataComponentType<D>> registerComponentType(String name,
			java.util.function.UnaryOperator<DataComponentType.Builder<D>> builder) {
			return register(name, () -> builder.apply(DataComponentType.builder()).build());
		}
	}
}
