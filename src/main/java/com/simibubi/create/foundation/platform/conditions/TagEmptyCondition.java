package com.simibubi.create.foundation.platform.conditions;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record TagEmptyCondition(TagKey<Item> tag) implements ICondition {

	public static final MapCodec<TagEmptyCondition> CODEC = ResourceLocation.CODEC.fieldOf("tag").xmap(id -> new TagEmptyCondition(TagKey.create(Registries.ITEM, id)), c -> c.tag().location());

	public TagEmptyCondition(ResourceLocation tag) {
		this(TagKey.create(Registries.ITEM, tag));
	}

	@Override
	public boolean test(IContext context) {
		return !context.isTagLoaded(tag);
	}

	@Override
	public MapCodec<? extends ICondition> codec() {
		return CODEC;
	}
}
