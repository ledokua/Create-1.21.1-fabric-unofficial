package com.simibubi.create.foundation.utility;

import net.minecraft.world.phys.AABB;

/** Constants NeoForge adds to {@link AABB}. */
public final class AABBs {
	private AABBs() {}

	public static final AABB INFINITE = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
}
