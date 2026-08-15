package com.simibubi.create.foundation.event.lifecycle;

import com.simibubi.create.foundation.event.bus.Event;

/** Lifecycle marker: common setup. Posted from Create.onInitialize after registration. Work runs inline. */
public class FMLCommonSetupEvent extends Event {

	public void enqueueWork(Runnable work) {
		work.run();
	}
}
