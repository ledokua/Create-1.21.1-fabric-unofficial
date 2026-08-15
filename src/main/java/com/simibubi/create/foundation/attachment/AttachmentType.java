package com.simibubi.create.foundation.attachment;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.simibubi.create.foundation.platform.ServerLifecycleHooks;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * A data-attachment type (Create-owned; upstream uses NeoForge's), backed by Fabric's attachment API. Because
 * Fabric's persistence codec can't see the target, values loaded from disk are kept as a {@link Pending}
 * NBT blob and turned into the real object with the holder on first {@code getData} — which is when
 * NeoForge's serializer would have run anyway.
 */
public final class AttachmentType<T> {

	public static <T> Builder<T> builder(Supplier<T> defaultValue) {
		return new Builder<>(defaultValue);
	}

	/** Deserialized-but-unbound value; replaced by the real one on first access. */
	private record Pending(CompoundTag tag) {}

	private final net.fabricmc.fabric.api.attachment.v1.AttachmentType<Object> fabric;
	private final Supplier<T> defaultValue;
	@Nullable
	private final IAttachmentSerializer<CompoundTag, T> serializer;

	private AttachmentType(ResourceLocation id, Supplier<T> defaultValue, @Nullable IAttachmentSerializer<CompoundTag, T> serializer, boolean copyOnDeath) {
		this.defaultValue = defaultValue;
		this.serializer = serializer;
		AttachmentRegistry.Builder<Object> builder = AttachmentRegistry.builder();
		if (serializer != null)
			builder.persistent(codec());
		if (copyOnDeath)
			builder.copyOnDeath();
		this.fabric = builder.buildAndRegister(id);
	}

	@SuppressWarnings("unchecked")
	private Codec<Object> codec() {
		return CompoundTag.CODEC.xmap(tag -> (Object) new Pending(tag), value -> {
			if (value instanceof Pending pending)
				return pending.tag();
			CompoundTag tag = serializer.write((T) value, registries());
			return tag == null ? new CompoundTag() : tag;
		});
	}

	private static HolderLookup.Provider registries() {
		var server = ServerLifecycleHooks.getCurrentServer();
		return server != null ? server.registryAccess() : RegistryAccess.EMPTY;
	}

	net.fabricmc.fabric.api.attachment.v1.AttachmentType<Object> fabric() {
		return fabric;
	}

	@SuppressWarnings("unchecked")
	T resolve(IAttachmentHolder holder, AttachmentTarget target) {
		Object value = target.getAttached(fabric);
		if (value == null) {
			T created = defaultValue.get();
			target.setAttached(fabric, created);
			return created;
		}
		if (value instanceof Pending pending) {
			T bound = serializer != null ? serializer.read(holder, pending.tag(), registries()) : defaultValue.get();
			target.setAttached(fabric, bound);
			return bound;
		}
		return (T) value;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	T unwrapIfBound(@Nullable Object value) {
		return value instanceof Pending || value == null ? null : (T) value;
	}

	public T getDefaultValue() {
		return defaultValue.get();
	}

	public static final class Builder<T> {
		private final Supplier<T> defaultValue;
		@Nullable
		private IAttachmentSerializer<CompoundTag, T> serializer;
		private boolean copyOnDeath;

		private Builder(Supplier<T> defaultValue) {
			this.defaultValue = defaultValue;
		}

		public Builder<T> serialize(IAttachmentSerializer<CompoundTag, T> serializer) {
			this.serializer = serializer;
			return this;
		}

		public Builder<T> copyOnDeath() {
			this.copyOnDeath = true;
			return this;
		}

		public AttachmentType<T> build(ResourceLocation id) {
			return new AttachmentType<>(id, defaultValue, serializer, copyOnDeath);
		}
	}
}
