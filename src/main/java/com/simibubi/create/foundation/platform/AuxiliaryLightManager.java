package com.simibubi.create.foundation.platform;

import net.minecraft.core.BlockPos;

/**
 * NeoForge's per-chunk auxiliary light storage (lets a block entity report a light level without a block
 * state property). No Fabric equivalent; {@link #getLightAt} returns 0 and {@link #setLightAt} is ignored,
 * so copycat blocks fall back to their default light. TODO(Phase 3): back with a chunk attachment + light
 * engine hook if copycat glow matters.
 */
public final class AuxiliaryLightManager {

	public static final AuxiliaryLightManager NONE = new AuxiliaryLightManager();

	private AuxiliaryLightManager() {}

	public void setLightAt(BlockPos pos, int value) {}

	public void removeLightAt(BlockPos pos) {}

	public int getLightAt(BlockPos pos) {
		return 0;
	}
}
