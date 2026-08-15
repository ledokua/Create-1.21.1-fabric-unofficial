package com.simibubi.create.foundation.mixin.extension;

import net.minecraft.nbt.CompoundTag;

/** Injected onto {@code net.minecraft.world.entity.Entity}; implemented by {@code EntityPersistentDataMixin}. */
public interface EntityExtension {

	/** Mod-private NBT that persists with the entity (NeoForge's {@code Entity#getPersistentData()}). */
	default CompoundTag getPersistentData() {
		throw new AbstractMethodError("EntityExtension#getPersistentData not implemented by mixin");
	}
}
