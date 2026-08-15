package com.simibubi.create.foundation.mixin.event;

import net.minecraft.world.entity.item.ItemEntity;

/** Duck interface implemented onto LivingEntity by {@link LivingEntityEventsMixin}. */
public interface DropCapture {
	/** Returns true if the drop was captured (for a pending LivingDropsEvent) instead of spawned. */
	boolean create$captureDrop(ItemEntity itemEntity);
}
