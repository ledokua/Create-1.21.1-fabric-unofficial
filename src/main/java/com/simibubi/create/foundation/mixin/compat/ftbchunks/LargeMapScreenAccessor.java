package com.simibubi.create.foundation.mixin.compat.ftbchunks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen;
import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;

/** Fabric replacement for upstream's ObfuscationReflectionHelper access to LargeMapScreen#regionPanel. */
@Mixin(value = LargeMapScreen.class, remap = false)
public interface LargeMapScreenAccessor {
	@Accessor("regionPanel")
	RegionMapPanel create$getRegionPanel();
}
