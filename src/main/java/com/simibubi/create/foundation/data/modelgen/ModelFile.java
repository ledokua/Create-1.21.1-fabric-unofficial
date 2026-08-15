package com.simibubi.create.foundation.data.modelgen;

import net.minecraft.resources.ResourceLocation;

/** Shell — see package docs. A reference to a model by location. */
public abstract class ModelFile {

	protected ResourceLocation location;

	protected ModelFile(ResourceLocation location) {
		this.location = location;
	}

	protected abstract boolean exists();

	public ResourceLocation getLocation() {
		return location;
	}

	public ResourceLocation getUncheckedLocation() {
		return location;
	}

	public static class UncheckedModelFile extends ModelFile {
		public UncheckedModelFile(String location) {
			this(ResourceLocation.parse(location));
		}

		public UncheckedModelFile(ResourceLocation location) {
			super(location);
		}

		@Override
		protected boolean exists() {
			return true;
		}
	}

	public static class ExistingModelFile extends ModelFile {
		public ExistingModelFile(ResourceLocation location, ExistingFileHelper existingHelper) {
			super(location);
		}

		@Override
		protected boolean exists() {
			return true;
		}
	}
}
