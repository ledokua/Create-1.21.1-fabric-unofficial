package com.simibubi.create.foundation.event.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a handler method (one parameter: the event) picked up by {@link IEventBus#register(Object)}. Kept
 * upstream-shaped so ~120 handlers port unchanged.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {

	EventPriority priority() default EventPriority.NORMAL;

	/** Whether the handler also runs for events already cancelled by an earlier listener. */
	boolean receiveCanceled() default false;
}
