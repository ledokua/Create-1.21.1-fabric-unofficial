package com.simibubi.create.foundation.event.client;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.DeltaTracker;

/** Per-frame hooks (before/after GameRenderer#render); fired from a GameRenderer mixin. */
public abstract class RenderFrameEvent extends Event {

	private final DeltaTracker partialTick;

	protected RenderFrameEvent(DeltaTracker partialTick) {
		this.partialTick = partialTick;
	}

	public DeltaTracker getPartialTick() {
		return partialTick;
	}

	public static class Pre extends RenderFrameEvent {
		public Pre(DeltaTracker partialTick) {
			super(partialTick);
		}
	}

	public static class Post extends RenderFrameEvent {
		public Post(DeltaTracker partialTick) {
			super(partialTick);
		}
	}
}
