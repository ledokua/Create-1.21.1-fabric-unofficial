package com.simibubi.create.foundation.client.command;

import java.util.IdentityHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.RegisterClientCommandsEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.flag.FeatureFlags;

/**
 * Port of NeoForge's {@code ClientCommandHandler}. Client commands are kept in their own
 * {@code CommandDispatcher<CommandSourceStack>}; {@code ClientPacketListener#sendCommand} is intercepted (see
 * {@code ClientCommandsMixin}) and, if the command fully parses here, it runs locally instead of going to the server.
 * The client tree is also merged into the server's suggestion tree so tab-completion works.
 * <p>
 * Registration happens once per connection (like NeoForge: {@code RegisterClientCommandsEvent} is fired when the
 * server's command tree arrives, with the connection's {@link CommandBuildContext}).
 */
@Environment(EnvType.CLIENT)
public final class ClientCommandHandler {

	private static CommandDispatcher<CommandSourceStack> commands = new CommandDispatcher<>();

	private ClientCommandHandler() {}

	public static CommandDispatcher<CommandSourceStack> getDispatcher() {
		return commands;
	}

	/** Rebuilds the client dispatcher and merges it into {@code serverCommands} (the suggestion tree). */
	public static void mergeServerCommands(RootCommandNode<SharedSuggestionProvider> serverCommands, @Nullable CommandBuildContext context) {
		if (context == null)
			context = CommandBuildContext.simple(Minecraft.getInstance().level != null
				? Minecraft.getInstance().level.registryAccess()
				: net.minecraft.core.RegistryAccess.EMPTY, FeatureFlags.DEFAULT_FLAGS);
		commands = new CommandDispatcher<>();
		try {
			CreateEventBus.BUS.post(new RegisterClientCommandsEvent(commands, context));
		} catch (Exception e) {
			Create.LOGGER.error("Failed to register client commands", e);
		}
		Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> copied = new IdentityHashMap<>();
		copied.put(commands.getRoot(), serverCommands);
		copy(commands.getRoot(), serverCommands, copied);
	}

	@SuppressWarnings("unchecked")
	private static void copy(CommandNode<CommandSourceStack> source, CommandNode<SharedSuggestionProvider> target,
		Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> copied) {
		for (CommandNode<CommandSourceStack> child : source.getChildren()) {
			CommandNode<SharedSuggestionProvider> existing = copied.get(child);
			if (existing == null) {
				ArgumentBuilder<SharedSuggestionProvider, ?> builder = (ArgumentBuilder<SharedSuggestionProvider, ?>) (ArgumentBuilder<?, ?>) child.createBuilder();
				builder.requires(s -> Minecraft.getInstance().player != null && child.canUse(ClientCommandSourceStack.of(Minecraft.getInstance())));
				if (builder.getCommand() != null)
					builder.executes(ctx -> 0);
				if ((Object) builder instanceof RequiredArgumentBuilder<?, ?> req && req.getSuggestionsProvider() != null) {
					RequiredArgumentBuilder<SharedSuggestionProvider, ?> required = (RequiredArgumentBuilder<SharedSuggestionProvider, ?>) req;
					required.suggests((ctx, sb) -> {
						// Re-run the client's suggestion provider against a client source.
						CommandSourceStack src = ClientCommandSourceStack.of(Minecraft.getInstance());
						ParseResults<CommandSourceStack> parse = commands.parse(ctx.getInput(), src);
						return commands.getCompletionSuggestions(parse, sb.getStart());
					});
				}
				if (builder.getRedirect() != null) {
					CommandNode<SharedSuggestionProvider> redirect = copied.get(builder.getRedirect());
					builder.redirect(redirect != null ? redirect : target); // best effort for forward references
				}
				existing = builder.build();
				copied.put(child, existing);
				CommandNode<SharedSuggestionProvider> already = target.getChild(existing.getName());
				if (already != null) {
					// A server command with the same name exists (e.g. /create, /c): merge our children into it.
					existing = already;
					copied.put(child, existing);
				} else {
					target.addChild(existing);
				}
			}
			if (child.getRedirect() == null && !child.getChildren().isEmpty())
				copy(child, existing, copied);
		}
	}

	/**
	 * @param command the command without a leading slash
	 * @return true if it was a client command and was handled (successfully or not)
	 */
	public static boolean runCommand(String command) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return false;
		StringReader reader = new StringReader(command);
		if (reader.canRead() && reader.peek() == '/')
			reader.skip();
		ClientCommandSourceStack source = ClientCommandSourceStack.of(mc);
		ParseResults<CommandSourceStack> parse = commands.parse(reader, source);
		if (parse.getContext().getNodes().isEmpty())
			return false; // not one of ours
		if (parse.getReader().canRead())
			return false; // trailing input we don't understand: probably a server command sharing the root literal (/create ...)
		ParsedCommandNode<CommandSourceStack> last = parse.getContext().getNodes().get(parse.getContext().getNodes().size() - 1);
		if (last.getNode().getCommand() == null && last.getNode().getRedirect() == null)
			return false; // nothing executable here (e.g. bare "/create") -> let the server answer
		try {
			commands.execute(parse);
		} catch (CommandSyntaxException e) {
			source.sendFailure(ComponentUtils.fromMessage(e.getRawMessage()));
			if (e.getInput() != null && e.getCursor() >= 0) {
				int cursor = Math.min(e.getInput().length(), e.getCursor());
				MutableComponent text = Component.empty().withStyle(net.minecraft.ChatFormatting.GRAY)
					.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + command)));
				if (cursor > 10)
					text.append(CommonComponents.ELLIPSIS);
				text.append(e.getInput().substring(Math.max(0, cursor - 10), cursor));
				if (cursor < e.getInput().length())
					text.append(Component.literal(e.getInput().substring(cursor)).withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.UNDERLINE));
				text.append(Component.translatable("command.context.here").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.ITALIC));
				source.sendFailure(text);
			}
		} catch (Exception e) {
			Create.LOGGER.error("Error executing client command \"{}\"", command, e);
			source.sendFailure(Component.translatable("command.failed"));
		}
		return true;
	}
}
