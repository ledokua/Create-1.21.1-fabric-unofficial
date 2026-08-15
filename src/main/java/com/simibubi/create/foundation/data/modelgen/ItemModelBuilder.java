package com.simibubi.create.foundation.data.modelgen;

import net.minecraft.resources.ResourceLocation;

/** Shell — see package docs. */
public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {

	public ItemModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
		super(outputLocation, existingFileHelper);
	}

	public OverrideBuilder override() {
		return new OverrideBuilder();
	}

	public OverrideBuilder override(int index) {
		return new OverrideBuilder();
	}

	public class OverrideBuilder {
		public OverrideBuilder model(ModelFile model) {
			return this;
		}

		public OverrideBuilder predicate(ResourceLocation key, float value) {
			return this;
		}

		public ItemModelBuilder end() {
			return ItemModelBuilder.this;
		}
	}
}
