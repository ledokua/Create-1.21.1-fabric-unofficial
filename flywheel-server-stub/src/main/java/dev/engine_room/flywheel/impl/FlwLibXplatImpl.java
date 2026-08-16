package dev.engine_room.flywheel.impl;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;

/** Dedicated-server stand-in; model baking never happens on a server. */
public class FlwLibXplatImpl implements FlwLibXplat {
	@Override
	public BakedModel getBakedModel(ModelManager manager, ResourceLocation location) {
		throw new UnsupportedOperationException("client-only");
	}

	@Override
	public SimpleModel buildBakedModelBuilder(BakedModelBuilder builder) {
		throw new UnsupportedOperationException("client-only");
	}

	@Override
	public SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
		throw new UnsupportedOperationException("client-only");
	}
}
