package com.simibubi.create.compat.farmersdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Upstream's Farmer's Delight hook checks the mushroom-colony age property so harvesters only cut mature colonies.
 * Not wired to FD's Fabric build yet (Phase 12); returns the vanilla-mushroom answer.
 */
public class FarmersDelightCompat {
	public static boolean shouldHarvestMushroom(Level level, BlockPos pos, BlockState state) {
		return true;
	}
}
