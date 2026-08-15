package com.simibubi.create.foundation.event.bus;

/** Marker + accessors for events whose default behaviour a handler can suppress. */
public interface ICancellableEvent {

	default boolean isCanceled() {
		return ((Event) this).isCanceledInternal();
	}

	default void setCanceled(boolean canceled) {
		((Event) this).setCanceledInternal(canceled);
	}
}
