package com.simibubi.create;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;
import com.simibubi.create.foundation.attachment.AttachmentType;
import com.simibubi.create.foundation.event.bus.IEventBus;

/** Data attachments. Fabric port: built directly on Create's {@link AttachmentType} (over Fabric's attachment API) instead of a NeoForge registry. */
public class AllAttachmentTypes {

	public static final Supplier<AttachmentType<MinecartController>> MINECART_CONTROLLER = lazy(
		AttachmentType.builder(() -> MinecartController.EMPTY).serialize(MinecartController.SERIALIZER).build(Create.asResource("minecart_controller"))
	);

	private static <T> Supplier<T> lazy(T value) {
		return () -> value;
	}

	@Internal
	public static void register(IEventBus modEventBus) {}
}
