package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import com.simibubi.create.foundation.model.BakedModelWrapper;

public class CustomRenderedItemModel extends BakedModelWrapper<BakedModel> {

	public CustomRenderedItemModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	/**
	 * Fabric's renderer (Indigo/Sodium) takes over ItemRenderer#render for non-vanilla-adapter models and only calls
	 * emitItemQuads, which skips the BEWLR branch: the item would render as its static model without the animated
	 * partials (potato cannon cog, extendo grip arms, symmetry wand core). Let vanilla reach the custom renderer.
	 */
	@Override
	public boolean isVanillaAdapter() {
		return true;
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext cameraItemDisplayContext, PoseStack mat,
		boolean leftHand) {
		// Super call returns originalModel, but we want to return this, else BEWLR
		// won't be used.
		super.applyTransform(cameraItemDisplayContext, mat, leftHand);
		return this;
	}

	public BakedModel getOriginalModel() {
		return originalModel;
	}

}
