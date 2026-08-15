package com.simibubi.create.foundation.data.modelgen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

/** Shell — see package docs. */
public class ExistingFileHelper {

	public boolean exists(ResourceLocation loc, PackType packType, String pathSuffix, String pathPrefix) {
		return true;
	}

	public boolean exists(ResourceLocation loc, IResourceType type) {
		return true;
	}

	public boolean isEnabled() {
		return false;
	}

	public interface IResourceType {}
}
