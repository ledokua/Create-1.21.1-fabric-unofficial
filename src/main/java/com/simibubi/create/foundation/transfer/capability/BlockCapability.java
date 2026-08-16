package com.simibubi.create.foundation.transfer.capability;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;

/**
 * A block-position-scoped capability lookup. Create-owned facade over Fabric's {@link BlockApiLookup} (see
 * PORTING_PLAN.md, "NeoForge → Fabric translation strategy") — {@code T} is the API, {@code C} the context
 * (a {@code Direction} for item/fluid handlers). Ported call sites use {@code find(level, pos, ctx)}, which
 * is Fabric's own method; this type mostly exists so upstream's static {@code Capabilities.X.BLOCK}
 * declarations and generic signatures read the same.
 */
public interface BlockCapability<T, C> extends BlockApiLookup<T, C> {

	/** The real Fabric lookup behind this facade (Fabric's {@code BlockApiCache} only accepts its own implementations). */
	BlockApiLookup<T, C> fabricLookup();
}
