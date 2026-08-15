package com.simibubi.create.foundation.event.lifecycle;

import com.simibubi.create.foundation.event.bus.Event;

/** Lifecycle marker: all mods initialised. Posted at the end of Create's init. Work runs inline. */
public class FMLLoadCompleteEvent extends Event {

	public void enqueueWork(Runnable work) {
		work.run();
	}
}
