package net.createmod.catnip.impl.client.render.model;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack.Pose;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/fabric/src/lib/java/dev/engine_room/flywheel/lib/model/baked/UniversalMeshEmitter.java
class UniversalMeshEmitter implements VertexConsumer {
	private final WrapperModel wrapperModel = new WrapperModel();

	@UnknownNullability
	private ShadeSeparatedBufferSource bufferSource;
	@UnknownNullability
	private RenderType defaultLayer;
	@UnknownNullability
	private VertexConsumer currentDelegate;

	public void prepare(ShadeSeparatedBufferSource bufferSource, RenderType defaultLayer) {
		this.bufferSource = bufferSource;
		this.defaultLayer = defaultLayer;
	}

	public void clear() {
		bufferSource = null;
		wrapperModel.setWrapped(null);
	}

	public BakedModel wrapModel(BakedModel model) {
		wrapperModel.setWrapped(model);
		return wrapperModel;
	}

	private void prepareForGeometry(RenderMaterial material) {
		BlendMode blendMode = material.blendMode();
		RenderType layer = blendMode == BlendMode.DEFAULT ? defaultLayer : blendMode.blockRenderLayer;
		boolean shade = !material.disableDiffuse();
		currentDelegate = bufferSource.getBuffer(layer, shade);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		currentDelegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		currentDelegate.setColor(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		currentDelegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		currentDelegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		currentDelegate.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		currentDelegate.setNormal(x, y, z);
		return this;
	}

	@Override
	public void addVertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
		currentDelegate.addVertex(x, y, z, color, u, v, overlay, light, normalX, normalY, normalZ);
	}

	@Override
	public void putBulkData(Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int light, int overlay) {
		currentDelegate.putBulkData(pose, bakedQuad, red, green, blue, alpha, light, overlay);
	}

	@Override
	public void putBulkData(Pose pose, BakedQuad bakedQuad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean readExistingColor) {
		currentDelegate.putBulkData(pose, bakedQuad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
	}

	private class WrapperModel extends ForwardingBakedModel {
		private final RenderContext.QuadTransform quadTransform = quad -> {
			UniversalMeshEmitter.this.prepareForGeometry(quad.material());
			return true;
		};

		public void setWrapped(@Nullable BakedModel wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			context.pushTransform(quadTransform);
			super.emitBlockQuads(level, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}
