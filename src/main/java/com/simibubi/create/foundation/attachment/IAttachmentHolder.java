package com.simibubi.create.foundation.attachment;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

/**
 * NeoForge's data-attachment accessors ({@code getData/setData/hasData/removeData}), interface-injected onto
 * Entity, BlockEntity, ChunkAccess and Level over Fabric's {@link AttachmentTarget}. Values persisted from
 * disk are bound to their holder lazily on first access (see {@link AttachmentType}).
 */
public interface IAttachmentHolder {

	private AttachmentTarget target() {
		return (AttachmentTarget) this;
	}

	default boolean hasData(AttachmentType<?> type) {
		return target().hasAttached(type.fabric());
	}

	default <T> T getData(AttachmentType<T> type) {
		return type.resolve(this, target());
	}

	default <T> Optional<T> getExistingData(AttachmentType<T> type) {
		return hasData(type) ? Optional.of(getData(type)) : Optional.empty();
	}

	@Nullable
	default <T> T setData(AttachmentType<T> type, T data) {
		Object previous = target().setAttached(type.fabric(), data);
		return type.unwrapIfBound(previous);
	}

	@Nullable
	default <T> T removeData(AttachmentType<T> type) {
		Object previous = target().removeAttached(type.fabric());
		return type.unwrapIfBound(previous);
	}
}
