package com.simibubi.create.foundation.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

/**
 * NeoForge's "plantable item" contract (Create-owned): items that can be planted by harvesters/villagers via
 * custom logic. Vanilla itself never calls these; Create's harvester and potato-cannon code do.
 */
public interface SpecialPlantable {

	boolean canPlacePlantAtPosition(ItemStack itemStack, LevelReader level, BlockPos pos, Direction direction);

	void spawnPlantAtPosition(ItemStack itemStack, LevelAccessor level, BlockPos pos, Direction direction);

	default boolean villagerCanPlantItem(Villager villager) {
		return false;
	}
}
