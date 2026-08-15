package com.tterrag.registrate.util;

import java.util.function.Consumer;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.bus.Event;
import com.tterrag.registrate.AbstractRegistrate;

/**
 * Upstream Registrate's one-shot mod-bus listener helper. Our "mod bus" is Create's bridge bus
 * ({@link CreateEventBus#BUS}), where lifecycle events (FMLClientSetupEvent etc.) are posted once by
 * {@code Create}/{@code CreateClient}; the listener drops itself after the first delivery.
 */
public final class OneTimeEventReceiver {

	private OneTimeEventReceiver() {}

	public static <T extends Event> void addModListener(AbstractRegistrate<?> owner, Class<T> eventType, Consumer<? super T> listener) {
		addListener(eventType, listener);
	}

	public static <T extends Event> void addListener(Class<T> eventType, Consumer<? super T> listener) {
		CreateEventBus.BUS.addListener(eventType, new Consumer<T>() {
			private boolean fired;

			@Override
			public void accept(T event) {
				if (fired)
					return;
				fired = true;
				listener.accept(event);
			}
		});
	}
}
