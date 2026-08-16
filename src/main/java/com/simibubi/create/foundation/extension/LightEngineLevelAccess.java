package com.simibubi.create.foundation.extension;

import net.minecraft.world.level.BlockGetter;

/** Duck on {@code LightEngine}: the level behind its chunk source (for position-aware light emission). */
public interface LightEngineLevelAccess {
	BlockGetter create$level();
}
