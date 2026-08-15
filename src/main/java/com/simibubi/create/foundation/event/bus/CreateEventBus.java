package com.simibubi.create.foundation.event.bus;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import com.simibubi.create.Create;

/**
 * The single event bus of the bridge (stands in for both NeoForge's game bus, {@code com.simibubi.create.foundation.event.bus.CreateEventBus.BUS},
 * and the mod bus). {@link #registerSubscribers()} registers every class in {@link CreateEventSubscribers}
 * whose {@code @EventBusSubscriber} side restriction matches the current physical side. Client-only
 * subscriber classes are referenced by name so a dedicated server never loads them.
 */
public final class CreateEventBus {

	public static final EventBus BUS = new EventBus();

	private CreateEventBus() {}

	public static void registerSubscribers() {
		EnvType env = FabricLoader.getInstance().getEnvironmentType();
		int count = 0;
		for (String className : CreateEventSubscribers.CLASSES) {
			Class<?> clazz;
			try {
				clazz = Class.forName(className, false, CreateEventBus.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Listed event subscriber class not found: " + className, e);
			}
			EventBusSubscriber annotation = clazz.getAnnotation(EventBusSubscriber.class);
			if (annotation == null)
				throw new IllegalStateException(className + " is listed as an event subscriber but lacks @EventBusSubscriber");
			if (!List.of(annotation.value()).contains(env))
				continue;
			// initialize now: side check passed, and static handlers may rely on the class's static init
			try {
				Class.forName(className, true, CreateEventBus.class.getClassLoader());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
			BUS.register(clazz);
			count++;
		}
		Create.LOGGER.debug("Registered {} event subscriber classes for side {}", count, env);
	}
}
