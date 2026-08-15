package com.simibubi.create.foundation.event.client;

import com.mojang.brigadier.CommandDispatcher;
import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

/**
 * Client command registration. NOTE: Fabric client commands use {@code FabricClientCommandSource}, not
 * {@code CommandSourceStack}, so Create's client command trees need adapting in Phase 13 (commands); until
 * then this is posted with a throwaway dispatcher so handlers run but nothing is registered.
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
