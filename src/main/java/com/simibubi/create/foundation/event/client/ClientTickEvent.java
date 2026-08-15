package com.simibubi.create.foundation.event.client;

import com.simibubi.create.foundation.event.bus.Event;

/** Client tick hooks; fired from Fabric's ClientTickEvents.START/END_CLIENT_TICK. */
public abstract class ClientTickEvent extends Event {

	public static class Pre extends ClientTickEvent {}

	public static class Post extends ClientTickEvent {}
}
