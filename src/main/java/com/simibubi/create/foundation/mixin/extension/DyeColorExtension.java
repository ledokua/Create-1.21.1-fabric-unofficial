package com.simibubi.create.foundation.mixin.extension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

/** Injected onto {@code DyeColor}: NeoForge's {@code getTag()} → the {@code c:dyes/<color>} convention tag. */
public interface DyeColorExtension {
	default TagKey<Item> getTag() {
		return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dyes/" + ((DyeColor) this).getName()));
	}
}
