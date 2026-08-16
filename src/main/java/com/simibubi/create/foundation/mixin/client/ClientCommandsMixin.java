package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.foundation.client.command.ClientCommandHandler;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;

/** NeoForge-style client command dispatch: intercepts outgoing commands and merges the client tree for suggestions. */
@Mixin(ClientPacketListener.class)
public abstract class ClientCommandsMixin {

	@Shadow
	private CommandDispatcher<SharedSuggestionProvider> commands;

	@Inject(method = "handleCommands", at = @At("TAIL"))
	private void create$mergeClientCommands(ClientboundCommandsPacket packet, CallbackInfo ci) {
		ClientPacketListener self = (ClientPacketListener) (Object) this;
		ClientCommandHandler.mergeServerCommands(commands.getRoot(), CommandBuildContext.simple(self.registryAccess(), self.enabledFeatures()));
	}

	@Inject(method = "sendCommand", at = @At("HEAD"), cancellable = true)
	private void create$runClientCommand(String command, CallbackInfo ci) {
		if (ClientCommandHandler.runCommand(command))
			ci.cancel();
	}

	@Inject(method = "sendUnsignedCommand", at = @At("HEAD"), cancellable = true)
	private void create$runClientCommandUnsigned(String command, CallbackInfoReturnable<Boolean> cir) {
		if (ClientCommandHandler.runCommand(command))
			cir.setReturnValue(true);
	}
}
