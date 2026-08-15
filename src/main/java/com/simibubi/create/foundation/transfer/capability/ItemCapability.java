package com.simibubi.create.foundation.transfer.capability;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;

/** Item-stack-scoped capability lookup; Create-owned facade over Fabric's {@link ItemApiLookup}. See {@link BlockCapability}. */
public interface ItemCapability<T, C> extends ItemApiLookup<T, C> {
}
