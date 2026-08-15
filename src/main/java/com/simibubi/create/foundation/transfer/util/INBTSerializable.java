package com.simibubi.create.foundation.transfer.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/**
 * Create-owned equivalent of the NBT-serializable contract Create's inventories/tanks implement (upstream:
 * NeoForge's {@code INBTSerializable}). See PORTING_PLAN.md, "NeoForge → Fabric translation strategy".
 */
public interface INBTSerializable<T extends Tag> {

	T serializeNBT(HolderLookup.Provider provider);

	void deserializeNBT(HolderLookup.Provider provider, T nbt);
}
