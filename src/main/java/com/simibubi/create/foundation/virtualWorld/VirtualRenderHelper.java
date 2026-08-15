package com.simibubi.create.foundation.virtualWorld;

import com.simibubi.create.foundation.model.data.ModelData;
import com.simibubi.create.foundation.model.data.ModelProperty;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ponder's NeoForge-only VirtualRenderHelper, on Create's ModelData. Marks model data as "virtual" (rendered
 * in a Ponder scene / schematic preview, not the real level) and caches Flywheel models for block states.
 */
public class VirtualRenderHelper {

	public static final ModelProperty<Boolean> VIRTUAL_PROPERTY = new ModelProperty<>();
	public static final ModelData VIRTUAL_DATA = ModelData.builder().with(VIRTUAL_PROPERTY, true).build();

	private static final RendererReloadCache<BlockState, Model> VIRTUAL_BLOCKS = new RendererReloadCache<>(
		state -> new BakedModelBuilder(Minecraft.getInstance().getBlockRenderer().getBlockModel(state)).build());

	public static boolean isVirtual(ModelData data) {
		return data.has(VIRTUAL_PROPERTY) && Boolean.TRUE.equals(data.get(VIRTUAL_PROPERTY));
	}

	public static Model blockModel(BlockState state) {
		return VIRTUAL_BLOCKS.get(state);
	}
}
