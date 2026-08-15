package com.simibubi.create.foundation.event.client;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;

/** Client join/leave hooks; fired from Fabric's ClientPlayConnectionEvents. */
public abstract class ClientPlayerNetworkEvent extends Event {

	@Nullable
	private final MultiPlayerGameMode multiPlayerGameMode;
	@Nullable
	private final LocalPlayer player;
	@Nullable
	private final Connection connection;

	protected ClientPlayerNetworkEvent(@Nullable MultiPlayerGameMode multiPlayerGameMode, @Nullable LocalPlayer player, @Nullable Connection connection) {
		this.multiPlayerGameMode = multiPlayerGameMode;
		this.player = player;
		this.connection = connection;
	}

	@Nullable
	public MultiPlayerGameMode getMultiPlayerGameMode() {
		return multiPlayerGameMode;
	}

	@Nullable
	public LocalPlayer getPlayer() {
		return player;
	}

	@Nullable
	public Connection getConnection() {
		return connection;
	}

	public static class LoggingIn extends ClientPlayerNetworkEvent {
		public LoggingIn(MultiPlayerGameMode multiPlayerGameMode, LocalPlayer player, Connection connection) {
			super(multiPlayerGameMode, player, connection);
		}
	}

	public static class LoggingOut extends ClientPlayerNetworkEvent {
		public LoggingOut(@Nullable MultiPlayerGameMode multiPlayerGameMode, @Nullable LocalPlayer player, @Nullable Connection connection) {
			super(multiPlayerGameMode, player, connection);
		}
	}
}
