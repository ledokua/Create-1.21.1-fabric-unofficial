package com.simibubi.create.foundation.data.modelgen;

import net.minecraft.resources.ResourceLocation;

/** Shell — see package docs. */
public abstract class ItemModelProvider extends ModelProvider<ItemModelBuilder> {

	public ItemModelProvider(String modid, ExistingFileHelper existingFileHelper) {
		super(modid, ITEM_FOLDER, ItemModelBuilder::new, existingFileHelper);
	}

	public ItemModelBuilder basicItem(net.minecraft.world.item.Item item) {
		return getBuilder(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath());
	}

	public ItemModelBuilder basicItem(ResourceLocation item) {
		return getBuilder(item.toString());
	}
}
