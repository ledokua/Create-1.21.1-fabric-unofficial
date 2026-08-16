package com.simibubi.create.foundation.event.client;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

/**
 * Client command registration, posted by {@code foundation.client.command.ClientCommandHandler} each time the server's
 * command tree arrives (NeoForge semantics: the dispatcher is a {@code CommandSourceStack} one, run locally).
 */
public class RegisterClientCommandsEvent extends Event {

	private final CommandDispatcher<CommandSourceStack> dispatcher;
	private final CommandBuildContext context;

	public RegisterClientCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
		this.dispatcher = dispatcher;
		this.context = context;
	}

	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return dispatcher;
	}

	public CommandBuildContext getBuildContext() {
		return context;
	}
}
