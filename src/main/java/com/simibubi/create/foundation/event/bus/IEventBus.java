package com.simibubi.create.foundation.event.bus;

import java.util.function.Consumer;

public interface IEventBus {

	<T extends Event> void addListener(Consumer<T> consumer);

	<T extends Event> void addListener(EventPriority priority, Consumer<T> consumer);

	<T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer);

	<T extends Event> void addListener(Class<T> eventType, Consumer<T> consumer);

	<T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer);

	/** Registers all {@link SubscribeEvent} methods: static ones for a {@code Class}, instance ones otherwise. */
	void register(Object target);

	void unregister(Object target);

	/** Dispatches to listeners of the event's class and its superclasses, by priority. Returns the event. */
	<T extends Event> T post(T event);
}
