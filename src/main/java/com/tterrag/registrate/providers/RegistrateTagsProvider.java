package com.tterrag.registrate.providers;

import net.minecraft.tags.TagKey;

/** Placeholder for Phase 11 — see {@link RegistrateBlockstateProvider}. */
public class RegistrateTagsProvider<T> {

	/** Matches upstream's bulk `addTag(TagKey)` call shape used in Create's CreateRegistrateTags. No-op for now. */
	public RegistrateTagsProvider<T>.TagAppender tag(TagKey<T> tag) {
		return new TagAppender();
	}

	public class TagAppender {
		public TagAppender add(@SuppressWarnings("unchecked") T... values) {
			return this;
		}
	}
}
