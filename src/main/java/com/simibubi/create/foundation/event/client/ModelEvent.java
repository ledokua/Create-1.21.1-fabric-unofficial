package com.simibubi.create.foundation.event.client;

import java.util.Map;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 * Model loading hooks. Fabric's model API (ModelLoadingPlugin) is callback-shaped rather than
 * map-shaped; Create's ModelSwapper is ported onto it directly (Phase 3), so this class only exists for
 * source compatibility and is not posted.
 */
public abstract class ModelEvent extends Event {

	public static class ModifyBakingResult extends ModelEvent {
		private final Map<ModelResourceLocation, BakedModel> models;

		public ModifyBakingResult(Map<ModelResourceLocation, BakedModel> models) {
			this.models = models;
		}

		public Map<ModelResourceLocation, BakedModel> getModels() {
			return models;
		}
	}
}
