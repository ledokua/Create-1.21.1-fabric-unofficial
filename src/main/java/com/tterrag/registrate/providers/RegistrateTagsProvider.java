package com.tterrag.registrate.providers;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;

/**
 * Shell — see {@link RegistrateBlockstateProvider}. Presents vanilla's TagsProvider surface
 * ({@code addTag(tag).add(...)}) so upstream's bulk tag callbacks compile.
 */
public abstract class RegistrateTagsProvider<T> {

	public abstract TagsProvider.TagAppender<T> addTag(TagKey<T> tag);

	public abstract HolderLookup.Provider getRegistries();
}
