package com.simibubi.create.foundation.mixin.extension;

import com.simibubi.create.foundation.platform.AuxiliaryLightManager;

import net.minecraft.core.BlockPos;

/** Injected onto {@code LevelReader}: NeoForge's auxiliary light manager accessor (see {@link AuxiliaryLightManager}). */
public interface LevelExtension {
	default AuxiliaryLightManager getAuxLightManager(BlockPos pos) {
		return AuxiliaryLightManager.NONE;
	}
}
