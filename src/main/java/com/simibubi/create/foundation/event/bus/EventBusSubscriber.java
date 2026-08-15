package com.simibubi.create.foundation.event.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.fabricmc.api.EnvType;

/**
 * Marks a class whose static {@link SubscribeEvent} methods should be registered at startup. Upstream
 * discovers these by classpath annotation scanning; here the class must also be listed in
 * {@link CreateEventBus#SUBSCRIBERS} (explicit, no scanning). {@link #value()} restricts to a physical side.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {

	EnvType[] value() default { EnvType.CLIENT, EnvType.SERVER };

	String modid() default "";

	Bus bus() default Bus.GAME;

	/** Upstream distinguishes the mod (lifecycle) and game buses; the bridge has one bus, this is informational. */
	enum Bus {
		GAME, MOD
	}
}
