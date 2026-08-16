package com.simibubi.create.foundation.extension;

import net.minecraft.world.inventory.Slot;

/** Injected onto {@code Slot}: NeoForge's {@code getSlotIndex()} (the container-side slot index). */
public interface SlotExtension {
	default int getSlotIndex() {
		return ((Slot) this).getContainerSlot();
	}
}
