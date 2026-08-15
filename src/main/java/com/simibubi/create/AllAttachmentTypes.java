package com.simibubi.create;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.content.contraptions.minecart.capability.MinecartController;

import com.simibubi.create.foundation.event.bus.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import com.simibubi.create.foundation.registry.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AllAttachmentTypes {
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Create.ID);

	public static final Supplier<AttachmentType<MinecartController>> MINECART_CONTROLLER = ATTACHMENT_TYPES.register(
		"minecart_controller", () -> AttachmentType.builder(() -> MinecartController.EMPTY).serialize(MinecartController.SERIALIZER).build()
	);

	@Internal
	public static void register(IEventBus modEventBus) {
		ATTACHMENT_TYPES.register(modEventBus);
	}
}
