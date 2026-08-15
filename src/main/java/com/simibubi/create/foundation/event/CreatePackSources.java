package com.simibubi.create.foundation.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.RepositorySource;

/** Collects RepositorySources added via {@link AddPackFindersEvent}; injected into the pack repository by a mixin. */
public final class CreatePackSources {

	private static final Map<PackType, List<RepositorySource>> SOURCES = Map.of(
		PackType.CLIENT_RESOURCES, new ArrayList<>(), PackType.SERVER_DATA, new ArrayList<>());

	private CreatePackSources() {}

	public static void add(PackType type, RepositorySource source) {
		SOURCES.get(type).add(source);
	}

	public static List<RepositorySource> get(PackType type) {
		return SOURCES.get(type);
	}
}
