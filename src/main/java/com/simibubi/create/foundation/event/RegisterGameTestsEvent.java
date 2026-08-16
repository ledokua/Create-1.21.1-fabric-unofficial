package com.simibubi.create.foundation.event;

import com.simibubi.create.foundation.event.bus.Event;

/**
 * Lifecycle event: register game test classes. Fabric's gametest API discovers tests via the
 * {@code fabric-gametest} entrypoint (see fabric.mod.json → CreateGameTests) instead; kept so upstream's handler compiles.
 */
public class RegisterGameTestsEvent extends Event {

	public void register(Class<?> testClass) {}

	public void register(Class<?> testClass, String... methodNames) {}
}
