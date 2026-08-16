package com.simibubi.create.foundation.client.command;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Port of NeoForge's {@code ClientCommandSourceStack}: a {@link CommandSourceStack} backed by the local player, so
 * Create's client command trees (typed on {@code CommandSourceStack}) can run client-side. Server-only accessors throw.
 */
@Environment(EnvType.CLIENT)
public class ClientCommandSourceStack extends CommandSourceStack {

	public ClientCommandSourceStack(LocalPlayer player) {
		super(player, player.position(), player.getRotationVector(), null, 0, player.getName().getString(), player.getDisplayName(), null, player);
	}

	public static ClientCommandSourceStack of(Minecraft mc) {
		return new ClientCommandSourceStack(mc.player);
	}

	@Override
	public void sendSuccess(Supplier<Component> message, boolean allowLogging) {
		Minecraft.getInstance().gui.getChat().addMessage(message.get());
	}

	@Override
	public void sendFailure(Component message) {
		Minecraft.getInstance().gui.getChat().addMessage(Component.literal("").append(message).withStyle(net.minecraft.ChatFormatting.RED));
	}

	@Override
	public void sendSystemMessage(Component message) {
		Minecraft.getInstance().gui.getChat().addMessage(message);
	}

	@Override
	public MinecraftServer getServer() {
		throw new UnsupportedOperationException("Attempted to get server in client command");
	}

	@Override
	public ServerLevel getLevel() {
		throw new UnsupportedOperationException("Attempted to get server level in client command");
	}

	public Level getUnsidedLevel() {
		return Minecraft.getInstance().level;
	}

	@Override
	public Vec3 getPosition() {
		return Minecraft.getInstance().player.position();
	}

	@Override
	public Vec2 getRotation() {
		return Minecraft.getInstance().player.getRotationVector();
	}
}
