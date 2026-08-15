package com.tterrag.registrate.util.entry;

import java.util.Optional;

import com.simibubi.create.foundation.transfer.fluid.BaseFlowingFluid;
import com.simibubi.create.foundation.transfer.fluid.FluidType;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class FluidEntry<T extends Fluid> extends RegistryEntry<Fluid, T> {

	public static <T extends Fluid> FluidEntry<T> unresolved(ResourceLocation id) {
		return new FluidEntry<>(id);
	}

	protected FluidEntry(ResourceLocation id) {
		super(Registries.FLUID, id);
	}

	@SuppressWarnings("unchecked")
	public <S extends BaseFlowingFluid> S getSource() {
		return (S) ((BaseFlowingFluid) get()).getSource();
	}

	public FluidType getType() {
		return ((BaseFlowingFluid) get()).getFluidType();
	}

	@SuppressWarnings("unchecked")
	public <B extends Block> Optional<B> getBlock() {
		Block block = get().defaultFluidState().createLegacyBlock().getBlock();
		return block == net.minecraft.world.level.block.Blocks.AIR ? Optional.empty() : Optional.of((B) block);
	}

	@SuppressWarnings("unchecked")
	public <I extends Item> Optional<I> getBucket() {
		Item bucket = get().getBucket();
		return bucket == Items.AIR ? Optional.empty() : Optional.of((I) bucket);
	}
}
