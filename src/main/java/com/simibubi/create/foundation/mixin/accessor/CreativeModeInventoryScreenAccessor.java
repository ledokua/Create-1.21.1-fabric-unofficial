package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

/** Used by the dev screenshot harness to open the creative inventory on a specific tab. */
@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenAccessor {
	@Accessor("selectedTab")
	static void create$setSelectedTab(CreativeModeTab tab) {
		throw new AssertionError();
	}

	@Invoker("selectTab")
	void create$selectTab(CreativeModeTab tab);
}
