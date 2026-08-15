package com.simibubi.create.foundation.fluid;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.FluidRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.simibubi.create.foundation.client.extensions.IClientFluidTypeExtensions;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;
import com.simibubi.create.foundation.transfer.fluid.FluidType;

@Environment(EnvType.CLIENT)
public class FluidRenderer {

	// ---- FluidStack box rendering: NeoForge Catnip's FluidRenderHelper#renderFluidBox(FluidStack, ...) — the Fabric
	// Catnip build only takes FluidVariant, which would lose Create's component-aware tinting (potion fluids). ----

	public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
		MultiBufferSource buffer, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, FluidRenderHelper.getFluidBuilder(buffer), ms, light, renderBottom, invertGasses);
	}

	public static void renderFluidBox(FluidStack fluidStack, float xMin, float yMin, float zMin, float xMax, float yMax, float zMax,
		VertexConsumer builder, PoseStack ms, int light, boolean renderBottom, boolean invertGasses) {
		Fluid fluid = fluidStack.getFluid();
		IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid);
		FluidType fluidAttributes = fluid.getFluidType();
		TextureAtlasSprite fluidTexture = Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(clientFluid.getStillTexture(fluidStack));

		int color = clientFluid.getTintColor(fluidStack);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLightLevel(fluidStack));
		light = (light & 0xF00000) | luminosity << 4;

		net.minecraft.world.phys.Vec3 center = new net.minecraft.world.phys.Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
		ms.pushPose();
		if (invertGasses && fluidAttributes.isLighterThanAir()) {
			ms.translate(center.x, center.y, center.z);
			ms.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180));
			ms.translate(-center.x, -center.y, -center.z);
		}

		for (Direction side : net.createmod.catnip.data.Iterate.directions) {
			if (side == Direction.DOWN && !renderBottom)
				continue;

			boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
			if (side.getAxis()
				.isHorizontal()) {
				if (side.getAxis() == Direction.Axis.X) {
					FluidRenderHelper.renderStillTiledFace(side, zMin, yMin, zMax, yMax, positive ? xMax : xMin,
						builder, ms, light, color, fluidTexture);
				} else {
					FluidRenderHelper.renderStillTiledFace(side, xMin, yMin, xMax, yMax, positive ? zMax : zMin,
						builder, ms, light, color, fluidTexture);
				}
			} else {
				FluidRenderHelper.renderStillTiledFace(side, xMin, zMin, xMax, zMax, positive ? yMax : yMin,
					builder, ms, light, color, fluidTexture);
			}
		}

		ms.popPose();
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, MultiBufferSource buffer, PoseStack ms, int light) {
		renderFluidStream(fluidStack, direction, radius, progress, inbound, FluidRenderHelper.getFluidBuilder(buffer), ms, light);
	}

	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
		boolean inbound, VertexConsumer builder, PoseStack ms, int light) {
		Fluid fluid = fluidStack.getFluid();
		IClientFluidTypeExtensions clientFluid = IClientFluidTypeExtensions.of(fluid);
		FluidType fluidAttributes = fluid.getFluidType();
		Function<ResourceLocation, TextureAtlasSprite> spriteAtlas = Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
		TextureAtlasSprite flowTexture = spriteAtlas.apply(clientFluid.getFlowingTexture(fluidStack));
		TextureAtlasSprite stillTexture = spriteAtlas.apply(clientFluid.getStillTexture(fluidStack));

		int color = clientFluid.getTintColor(fluidStack);
		int blockLightIn = (light >> 4) & 0xF;
		int luminosity = Math.max(blockLightIn, fluidAttributes.getLightLevel(fluidStack));
		light = (light & 0xF00000) | luminosity << 4;

		if (inbound)
			direction = direction.getOpposite();

		var msr = TransformStack.of(ms);
		ms.pushPose();
		msr.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(direction))
			.rotateXDegrees(direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)
			.uncenter();
		ms.translate(.5, 0, .5);

		float h = radius;
		float hMin = -radius;
		float hMax = radius;
		float y = inbound ? 1 : .5f;
		float yMin = y - Mth.clamp(progress * .5f, 0, 1);
		float yMax = y;

		for (int i = 0; i < 4; i++) {
			ms.pushPose();
			renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, hMax, yMax, h, builder, ms, light, color, flowTexture);
			ms.popPose();
			msr.rotateYDegrees(90);
		}

		if (progress != 1)
			FluidRenderHelper.renderStillTiledFace(Direction.DOWN, hMin, hMin, hMax, hMax, yMin, builder, ms, light, color, stillTexture);

		ms.popPose();
	}

	public static void renderFlowingTiledFace(Direction dir, float left, float down, float right, float up,
		float depth, VertexConsumer builder, PoseStack ms, int light, int color, TextureAtlasSprite texture) {
		FluidRenderHelper.renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 0.5f);
	}

}
