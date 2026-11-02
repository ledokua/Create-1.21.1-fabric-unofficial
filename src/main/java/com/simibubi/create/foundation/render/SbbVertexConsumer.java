package com.simibubi.create.foundation.render;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Fabric port: Catnip's {@link ShadedBlockSbbBuilder} only accepts whole quads ({@code putBulkData}), but Fabric's
 * renderers (Indigo, Sodium's FRAPI) write the quads of non-vanilla-adapter models (Create's data-aware models, other
 * mods' FRAPI models) vertex by vertex. That threw "ShadedBlockSbbBuilder only supports putBulkData!" whenever a
 * contraption or schematic holding such a block was buffered with Flywheel off (e.g. under Iris shaders).
 * <p>
 * Whole quads pass straight through. Per-vertex writes go into the builder's own BufferBuilder via
 * {@link ShadedBlockSbbBuilder#unwrap(boolean)}, which keeps its shaded/unshaded vertex separation;
 * {@code FabricModelBridge} reports each emitted quad's shade flag through {@link #active()}.
 */
public final class SbbVertexConsumer implements VertexConsumer {

	private static final ThreadLocal<SbbVertexConsumer> ACTIVE = new ThreadLocal<>();

	private final ShadedBlockSbbBuilder sbb;
	private boolean quadShade = true;
	private VertexConsumer vertexTarget;

	public SbbVertexConsumer(ShadedBlockSbbBuilder sbb) {
		this.sbb = sbb;
	}

	/** Starts the builder and makes this the consumer that quad shade flags on this thread are reported to. */
	public void begin() {
		sbb.begin();
		quadShade = true;
		vertexTarget = null;
		ACTIVE.set(this);
	}

	public void release() {
		ACTIVE.remove();
		vertexTarget = null;
	}

	/** The consumer currently buffering blocks on this thread, if any. */
	@Nullable
	public static SbbVertexConsumer active() {
		return ACTIVE.get();
	}

	/** Shade flag of the quad about to be written vertex by vertex ({@link BakedQuad#isShade()}; vanilla's default is true). */
	public void setQuadShade(boolean shade) {
		quadShade = shade;
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight,
		int packedOverlay) {
		sbb.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha,
		int[] lightmap, int packedOverlay, boolean readAlpha) {
		sbb.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, readAlpha);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		vertexTarget = sbb.unwrap(quadShade);
		vertexTarget.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		vertexTarget.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		vertexTarget.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		vertexTarget.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		vertexTarget.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		vertexTarget.setNormal(x, y, z);
		return this;
	}
}
