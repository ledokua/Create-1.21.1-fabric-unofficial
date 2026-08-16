package dev.engine_room.flywheel.impl;

import java.util.Deque;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;

import net.minecraft.client.model.geom.ModelPart;

/** Dedicated-server stand-in for Flywheel's lib link; rendering-related members are never reached on a server. */
public class FlwLibLinkImpl implements FlwLibLink {
	private static final Logger LOGGER = LoggerFactory.getLogger("flywheel-server-stub");

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public PoseTransformStack getPoseTransformStackOf(PoseStack stack) {
		throw new UnsupportedOperationException("client-only");
	}

	@Override
	public Map<String, ModelPart> getModelPartChildren(ModelPart part) {
		throw new UnsupportedOperationException("client-only");
	}

	@Override
	public void compileModelPart(ModelPart part, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, int color) {
		throw new UnsupportedOperationException("client-only");
	}

	@Override
	public Deque<PoseStack.Pose> getPoseStack(PoseStack stack) {
		throw new UnsupportedOperationException("client-only");
	}

	@Override
	public boolean isIrisLoaded() {
		return false;
	}

	@Override
	public boolean isOptifineInstalled() {
		return false;
	}

	@Override
	public boolean isShaderPackInUse() {
		return false;
	}

	@Override
	public boolean isRenderingShadowPass() {
		return false;
	}
}
