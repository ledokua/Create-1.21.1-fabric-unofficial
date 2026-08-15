package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;


public class FixLightingCommand {
	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("fixLighting")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				// NeoForge-only: experimental light pipeline toggle has no Fabric equivalent
				Minecraft.getInstance().levelRenderer.allChanged();

				ctx.getSource().sendSuccess(() -> Component.literal("NeoForge's experimental block rendering pipeline is now enabled."), true);
				return Command.SINGLE_SUCCESS;
			});
	}
}
