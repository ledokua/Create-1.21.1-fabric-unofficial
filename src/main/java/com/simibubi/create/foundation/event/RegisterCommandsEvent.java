package com.simibubi.create.foundation.event;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/** Fired from Fabric's CommandRegistrationCallback. */
public class RegisterCommandsEvent extends Event {

	private final CommandDispatcher<CommandSourceStack> dispatcher;
	private final CommandBuildContext context;
	private final Commands.CommandSelection commandSelection;

	public RegisterCommandsEvent(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection commandSelection) {
		this.dispatcher = dispatcher;
		this.context = context;
		this.commandSelection = commandSelection;
	}

	public CommandDispatcher<CommandSourceStack> getDispatcher() {
		return dispatcher;
	}

	public CommandBuildContext getBuildContext() {
		return context;
	}

	public Commands.CommandSelection getCommandSelection() {
		return commandSelection;
	}
}
