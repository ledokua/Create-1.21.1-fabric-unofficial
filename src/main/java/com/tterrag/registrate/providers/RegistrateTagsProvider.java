package com.tterrag.registrate.providers;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;

/**
 * Shell — see {@link RegistrateBlockstateProvider}. Presents upstream's intrinsic tag appender surface
 * ({@code addTag(tag).add(entries...)}) so bulk tag callbacks compile; nothing is recorded.
 */
public abstract class RegistrateTagsProvider<T> {

	public TagAppender<T> addTag(TagKey<T> tag) {
		return new TagAppender<>();
	}

	public TagAppender<T> tag(TagKey<T> tag) {
		return addTag(tag);
	}

	public abstract HolderLookup.Provider getRegistries();

	public static final class TagAppender<T> {
		@SafeVarargs
		public final TagAppender<T> add(T... values) {
			return this;
		}

		public TagAppender<T> add(net.minecraft.resources.ResourceKey<T> key) {
			return this;
		}

		@SafeVarargs
		public final TagAppender<T> add(net.minecraft.resources.ResourceKey<T>... keys) {
			return this;
		}

		public TagAppender<T> addOptional(net.minecraft.resources.ResourceLocation id) {
			return this;
		}

		public TagAppender<T> addTag(TagKey<T> tag) {
			return this;
		}

		public TagAppender<T> addOptionalTag(net.minecraft.resources.ResourceLocation id) {
			return this;
		}

		public TagAppender<T> replace() {
			return this;
		}

		public TagAppender<T> replace(boolean value) {
			return this;
		}
	}
}
