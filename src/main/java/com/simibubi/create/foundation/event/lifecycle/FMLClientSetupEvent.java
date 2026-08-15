package com.simibubi.create.foundation.event.lifecycle;

import com.simibubi.create.foundation.event.bus.Event;

/** Lifecycle marker: client setup. Posted from CreateClient.onInitializeClient. Work runs inline. */
public class FMLClientSetupEvent extends Event {

	public void enqueueWork(Runnable work) {
		work.run();
	}
}
