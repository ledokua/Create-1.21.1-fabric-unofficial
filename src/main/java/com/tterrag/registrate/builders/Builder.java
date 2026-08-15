package com.tterrag.registrate.builders;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Contract every concrete builder (block/item/entity/...) satisfies. Mirrors upstream Registrate's
 * {@code Builder} interface signature-for-signature (so Create's call sites — e.g. assigning
 * {@code .register()}'s result straight to a {@code BlockEntry<T>} field — carry over unchanged), with the
 * deferred-registration bookkeeping swapped from NeoForge's {@code RegisterEvent} to
 * {@code AbstractRegistrate}'s own ordered flush.
 *
 * @param <R> base registry type (e.g. {@code Block})
 * @param <T> concrete type being built (e.g. {@code ShaftBlock})
 * @param <P> parent: {@code AbstractRegistrate} for top-level builders, the enclosing builder for nested
 *            ones like {@code BlockBuilder.item()}
 * @param <S> self type, for fluent chaining
 */
public interface Builder<R, T extends R, P, S extends Builder<R, T, P, S>> extends NonNullSupplier<RegistryEntry<R, T>> {

	/** Files this entry for registration (construction is deferred to the owner's flush) and returns its handle. */
	RegistryEntry<R, T> register();

	AbstractRegistrate<?> getOwner();

	P getParent();

	String getName();

	ResourceKey<? extends Registry<R>> getRegistryKey();

	/** The handle for this builder's entry. Only valid after {@link #register()}/{@link #build()}. */
	@Override
	default RegistryEntry<R, T> get() {
		return getOwner().<R, T>get(getName(), getRegistryKey());
	}

	/** The built object itself. Only valid after the owner's registration flush has run. */
	default T getEntry() {
		return get().get();
	}

	NonNullSupplier<T> asSupplier();

	/**
	 * Datagen hook. Stub for now (Phase 0b): the callback is accepted and dropped so ported call sites
	 * compile; nothing runs. Real datagen wiring is Phase 11 — see PORTING_PLAN.md.
	 */
	@SuppressWarnings("unchecked")
	default <D> S setData(ProviderType<? extends D> type, NonNullBiConsumer<DataGenContext<R, T>, D> cons) {
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	default <D> S addMiscData(ProviderType<? extends D> type, NonNullConsumer<? extends D> cons) {
		return (S) this;
	}

	/** Runs once, right after this entry resolves during the owner's flush. */
	@SuppressWarnings("unchecked")
	default S onRegister(NonNullConsumer<? super T> callback) {
		getOwner().<R, T>addRegisterCallback(getName(), getRegistryKey(), callback);
		return (S) this;
	}

	/**
	 * Runs after this entry resolves AND the given other registry has fully flushed. Upstream needs this
	 * because registries fire as separate events; our flush is ordered (see {@code AbstractRegistrate}), so
	 * this is the same mechanism, just checked against the flush's completed set.
	 */
	default <OR> S onRegisterAfter(ResourceKey<? extends Registry<OR>> dependencyType, NonNullConsumer<? super T> callback) {
		return onRegister(e -> {
			if (getOwner().isRegistered(dependencyType)) {
				callback.accept(e);
			} else {
				getOwner().addRegisterCallback(dependencyType, () -> callback.accept(e));
			}
		});
	}

	/** Applies a reusable partial-configuration function to this builder. */
	default <R2, T2 extends R2, P2, S2 extends Builder<R2, T2, P2, S2>> S2 transform(NonNullFunction<S, S2> func) {
		@SuppressWarnings("unchecked")
		S self = (S) this;
		return func.apply(self);
	}

	/** Registers this entry (result discarded) and returns to the parent builder/registrate. */
	default P build() {
		register();
		return getParent();
	}
}
