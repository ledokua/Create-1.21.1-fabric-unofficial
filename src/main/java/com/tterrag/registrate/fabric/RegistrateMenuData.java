package com.tterrag.registrate.fabric;

import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Opaque extra-data payload for menus opened with NeoForge-style "write a buffer on the server, read it on
 * the client" semantics ({@code IMenuTypeExtension} / {@code IPlayerExtension.openMenu(provider, buf ->
 * ...)}), carried over Fabric's typed {@code ExtendedScreenHandlerType}. Keeps every {@code
 * ForgeMenuFactory}-shaped call site upstream working unchanged: the client factory still receives a
 * {@link RegistryFriendlyByteBuf} to read from.
 * <p>
 * Server-side, Create's {@code openMenu(player, buf -> ...)} call sites need a {@code MenuProvider} that
 * implements Fabric's {@code ExtendedScreenHandlerFactory<RegistrateMenuData>} and returns
 * {@link #write(RegistryAccess, Consumer)} — that helper is Phase 1 work (see PORTING_PLAN.md).
 */
public record RegistrateMenuData(byte[] bytes) {

	public static final StreamCodec<RegistryFriendlyByteBuf, RegistrateMenuData> CODEC = StreamCodec.of(
		(buf, data) -> buf.writeByteArray(data.bytes()),
		buf -> new RegistrateMenuData(buf.readByteArray()));

	/** Server side: run the upstream-style buffer writer and capture what it wrote. */
	public static RegistrateMenuData write(RegistryAccess registryAccess, Consumer<RegistryFriendlyByteBuf> writer) {
		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
		try {
			writer.accept(buf);
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			return new RegistrateMenuData(bytes);
		} finally {
			buf.release();
		}
	}

	/** Client side: a readable buffer over the captured bytes. Caller owns the buffer's lifecycle. */
	public RegistryFriendlyByteBuf read(RegistryAccess registryAccess) {
		return new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), registryAccess);
	}
}
