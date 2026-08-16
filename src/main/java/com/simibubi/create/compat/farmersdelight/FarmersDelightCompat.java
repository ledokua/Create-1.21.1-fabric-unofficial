package com.simibubi.create.compat.farmersdelight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Upstream: harvesters don't cut mushrooms growing on Farmer's Delight rich soil (they'd regrow into colonies).
 * Resolved by block id ({@code farmersdelight:rich_soil}, same id in FD Refabricated) so no compile dependency is needed.
 */
public class FarmersDelightCompat {
	private static final ResourceLocation RICH_SOIL = ResourceLocation.fromNamespaceAndPath("farmersdelight", "rich_soil");

	public static boolean shouldHarvestMushroom(Level world, BlockPos pos, BlockState state) {
		return !BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos.below()).getBlock()).equals(RICH_SOIL);
	}
}
