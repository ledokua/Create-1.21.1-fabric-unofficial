package com.simibubi.create.foundation.render;

import java.util.Arrays;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Fabric port: forwards everything to {@code delegate} but writes full-bright light, whatever light the renderer
 * computed. Used for block models drawn in GUIs (Catnip's {@code GuiGameElement}: JEI/EMI recipe animations, Create's
 * screens), which are meant to be full-bright. Fabric's own renderer (Indigo) already gives full-bright there, but
 * Sodium lights those blocks itself and treats the inside of a solid block as dark, so faces it samples there (most of
 * a press or basin; all of it with Flywheel off) came out black.
 */
public final class FullBrightVertexConsumer implements VertexConsumer {

	private static final int FULL_BRIGHT_U = LightTexture.FULL_BRIGHT & 0xFFFF;
	private static final int FULL_BRIGHT_V = LightTexture.FULL_BRIGHT >>> 16;

	private final VertexConsumer delegate;

	public FullBrightVertexConsumer(VertexConsumer delegate) {
		this.delegate = delegate;
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		delegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		delegate.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		delegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		delegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		delegate.setUv2(FULL_BRIGHT_U, FULL_BRIGHT_V);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		delegate.setNormal(x, y, z);
		return this;
	}

	@Override
	public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight,
		float normalX, float normalY, float normalZ) {
		delegate.addVertex(x, y, z, color, u, v, packedOverlay, LightTexture.FULL_BRIGHT, normalX, normalY, normalZ);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight,
		int packedOverlay) {
		delegate.putBulkData(pose, quad, red, green, blue, alpha, LightTexture.FULL_BRIGHT, packedOverlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha,
		int[] lightmap, int packedOverlay, boolean readAlpha) {
		int[] fullBright = new int[lightmap.length];
		Arrays.fill(fullBright, LightTexture.FULL_BRIGHT);
		delegate.putBulkData(pose, quad, brightness, red, green, blue, alpha, fullBright, packedOverlay, readAlpha);
	}
}
