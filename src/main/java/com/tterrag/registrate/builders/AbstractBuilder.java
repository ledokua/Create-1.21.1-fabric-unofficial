package com.tterrag.registrate.builders;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.LazyRegistryEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Base plumbing shared by every concrete builder: owner/parent/name/registryKey tracking, the
 * {@link #register()} dispatch through {@link BuilderCallback}, and tag/lang assignment. Subclasses provide
 * {@link #createEntry()} — the actual object construction, invoked lazily by the owner's flush.
 */
public abstract class AbstractBuilder<R, T extends R, P, S extends Builder<R, T, P, S>> implements Builder<R, T, P, S> {

	private final AbstractRegistrate<?> owner;
	private final P parent;
	private final String name;
	private final ResourceKey<? extends Registry<R>> registryKey;
	protected final BuilderCallback callback;

	/** Supplier that forgets this builder once resolved, so captured lambdas don't pin the builder forever. */
	private final LazyRegistryEntry<R, T> safeSupplier = new LazyRegistryEntry<>(this);

	protected AbstractBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		ResourceKey<? extends Registry<R>> registryKey) {
		this.owner = owner;
		this.parent = parent;
		this.name = name;
		this.callback = callback;
		this.registryKey = registryKey;
	}

	/** Actually constructs the object. Called once, lazily, when the owner's flush reaches this entry. */
	protected abstract T createEntry();

	/** Which {@link RegistryEntry} subtype to hand back; override to return e.g. a {@code BlockEntry}. */
	protected RegistryEntry<R, T> createEntryWrapper(ResourceLocation id) {
		return RegistryEntry.unresolved(registryKey, id);
	}

	@Override
	public RegistryEntry<R, T> register() {
		return callback.accept(name, registryKey, this, this::createEntry, this::createEntryWrapper);
	}

	@Override
	public AbstractRegistrate<?> getOwner() {
		return owner;
	}

	@Override
	public P getParent() {
		return parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ResourceKey<? extends Registry<R>> getRegistryKey() {
		return registryKey;
	}

	@Override
	public NonNullSupplier<T> asSupplier() {
		return safeSupplier;
	}

	/**
	 * Tag membership is data (a JSON file listing this entry's id), not runtime state — we vendor upstream's
	 * pre-generated tag JSON directly (PORTING_PLAN.md, Phase 0b/11), so this only needs to exist and
	 * type-check for the ~245 ported call sites that chain it. No-op. Concrete builders expose the
	 * upstream-shaped {@code tag(TagKey<Block>...)} overload on top of this.
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	protected final S tagInternal(TagKey<R>... tags) {
		return (S) this;
	}

	/** Same reasoning as {@link #tagInternal} — lang entries are vendored data. No-op. */
	@SuppressWarnings("unchecked")
	public S lang(String value) {
		return (S) this;
	}
}
