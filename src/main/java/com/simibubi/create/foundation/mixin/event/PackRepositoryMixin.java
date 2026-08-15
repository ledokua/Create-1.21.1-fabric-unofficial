package com.simibubi.create.foundation.mixin.event;

import java.util.LinkedHashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.CreatePackSources;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;

/** Adds the sources collected through {@code AddPackFindersEvent} (Create's runtime-generated data pack) to the right repository. */
@Mixin(PackRepository.class)
public class PackRepositoryMixin {
	@Shadow
	@Final
	@Mutable
	private Set<RepositorySource> sources;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void create$addSources(RepositorySource[] sourcesArg, CallbackInfo ci) {
		PackType type = null;
		for (RepositorySource source : sourcesArg) {
			if (source instanceof ServerPacksSource) {
				type = PackType.SERVER_DATA;
				break;
			}
			if (source.getClass().getName().equals("net.minecraft.client.resources.ClientPackSource")) {
				type = PackType.CLIENT_RESOURCES;
				break;
			}
		}
		if (type == null || CreatePackSources.get(type).isEmpty())
			return;
		Set<RepositorySource> extended = new LinkedHashSet<>(sources);
		extended.addAll(CreatePackSources.get(type));
		sources = extended;
	}
}
