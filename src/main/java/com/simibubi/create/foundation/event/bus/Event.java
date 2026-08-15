package com.simibubi.create.foundation.event.bus;

/**
 * Base class of the Create-owned event bridge (see PORTING_PLAN.md, "NeoForge → Fabric translation
 * strategy"). Event classes under {@code com.simibubi.create.foundation.event.*} mirror the NeoForge events
 * Create's handlers were written against, exposing exactly the members those handlers use; they're posted
 * on {@link CreateEventBus#BUS} from Fabric callbacks or small mixins.
 */
public abstract class Event {

	private boolean canceled;

	/** Only meaningful for {@link ICancellableEvent}s. */
	boolean isCanceledInternal() {
		return canceled;
	}

	void setCanceledInternal(boolean canceled) {
		this.canceled = canceled;
	}
}
