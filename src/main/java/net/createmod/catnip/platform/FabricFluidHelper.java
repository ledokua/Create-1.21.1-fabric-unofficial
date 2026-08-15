package net.createmod.catnip.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.platform.services.ModFluidHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;

public class FabricFluidHelper implements ModFluidHelper<FluidVariant> {
	@Environment(EnvType.CLIENT)
	@Override
	public int getColor(FluidVariant variant, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
		return FluidVariantRendering.getColor(variant, level, pos);
	}

	@Override
	public int getLuminosity(FluidVariant variant) {
		return FluidVariantAttributes.getLuminance(variant);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public TextureAtlasSprite getStillTexture(FluidVariant variant) {
		return FluidVariantRendering.getSprite(variant);
	}

	@Override
	public boolean isLighterThanAir(FluidVariant variant) {
		return FluidVariantAttributes.isLighterThanAir(variant);
	}

	@Override
	public FluidVariant toStack(FluidState state) {
		return FluidVariant.of(state.getType());
	}
}
