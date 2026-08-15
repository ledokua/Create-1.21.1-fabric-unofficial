package com.simibubi.create.foundation.client.extensions;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;

/**
 * Client-side fluid appearance (Create-owned; was NeoForge's FluidType client extension). {@link #of(Fluid)}
 * first checks Create's own registrations, then adapts whatever Fabric's {@link FluidRenderHandlerRegistry}
 * knows — so other mods' fluids render correctly in Create's pipes/tanks/particles.
 */
public interface IClientFluidTypeExtensions {

	IClientFluidTypeExtensions DEFAULT = new IClientFluidTypeExtensions() {};

	static IClientFluidTypeExtensions of(FluidState state) {
		return of(state.getType());
	}

	static IClientFluidTypeExtensions of(Fluid fluid) {
		IClientFluidTypeExtensions own = ClientExtensionsManager.FLUID_EXTENSIONS.get(fluid);
		if (own != null)
			return own;
		FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
		return handler == null ? DEFAULT : new FabricAdapter(fluid, handler);
	}

	default int getTintColor() {
		return 0xFFFFFFFF;
	}

	default int getTintColor(FluidStack stack) {
		return getTintColor();
	}

	default int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
		return getTintColor();
	}

	default ResourceLocation getStillTexture() {
		return ResourceLocation.withDefaultNamespace("block/water_still");
	}

	default ResourceLocation getStillTexture(FluidStack stack) {
		return getStillTexture();
	}

	default ResourceLocation getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
		return getStillTexture();
	}

	default ResourceLocation getFlowingTexture() {
		return ResourceLocation.withDefaultNamespace("block/water_flow");
	}

	default ResourceLocation getFlowingTexture(FluidStack stack) {
		return getFlowingTexture();
	}

	default ResourceLocation getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
		return getFlowingTexture();
	}

	@Nullable
	default ResourceLocation getOverlayTexture() {
		return null;
	}

	/** Adapts a Fabric fluid render handler (sprites + colour) to this interface. */
	final class FabricAdapter implements IClientFluidTypeExtensions {
		private final Fluid fluid;
		private final FluidRenderHandler handler;

		FabricAdapter(Fluid fluid, FluidRenderHandler handler) {
			this.fluid = fluid;
			this.handler = handler;
		}

		private TextureAtlasSprite[] sprites(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
			return handler.getFluidSprites(level, pos, fluid.defaultFluidState());
		}

		@Override
		public int getTintColor() {
			return handler.getFluidColor(null, null, fluid.defaultFluidState()) | 0xFF000000;
		}

		@Override
		public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
			return handler.getFluidColor(getter, pos, state) | 0xFF000000;
		}

		@Override
		public ResourceLocation getStillTexture() {
			return sprites(null, null)[0].contents().name();
		}

		@Override
		public ResourceLocation getFlowingTexture() {
			return sprites(null, null)[1].contents().name();
		}
	}
}
