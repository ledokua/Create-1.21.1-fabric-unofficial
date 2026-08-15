package com.simibubi.create.foundation.platform;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

/**
 * Brigadier argument for an enum's constants (NeoForge ships one; vanilla doesn't). Note: like upstream's,
 * this needs registering with the argument-type registry to be network-syncable — Create's commands use it
 * server-side only, where a plain (unregistered) argument type still works for parsing.
 */
public class EnumArgument<T extends Enum<T>> implements ArgumentType<T> {

	private static final DynamicCommandExceptionType INVALID_ENUM = new DynamicCommandExceptionType(found -> Component.literal("Invalid enum: " + found));
	private final Class<T> enumClass;

	public static <R extends Enum<R>> EnumArgument<R> enumArgument(Class<R> enumClass) {
		return new EnumArgument<>(enumClass);
	}

	private EnumArgument(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	@Override
	public T parse(StringReader reader) throws CommandSyntaxException {
		String name = reader.readUnquotedString();
		try {
			return Enum.valueOf(enumClass, name);
		} catch (IllegalArgumentException e) {
			throw INVALID_ENUM.createWithContext(reader, name);
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(Stream.of(enumClass.getEnumConstants()).map(Enum::name), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return Stream.of(enumClass.getEnumConstants()).map(Enum::name).toList();
	}

	public Class<T> getEnumClass() {
		return enumClass;
	}
}
