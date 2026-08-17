package com.simibubi.create.foundation.mixin.compat.ftbchunks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.ftb.mods.ftbchunks.client.gui.RegionMapPanel;

@Mixin(value = RegionMapPanel.class, remap = false)
public interface RegionMapPanelAccessor {
	@Accessor("regionMinX")
	int create$getRegionMinX();

	@Accessor("regionMinZ")
	int create$getRegionMinZ();
}
