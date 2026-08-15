package com.simibubi.create.foundation.transfer.capability;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.transfer.fluid.IFluidHandler;
import com.simibubi.create.foundation.transfer.item.IItemHandler;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registration handle passed to Create's static {@code registerCapabilities(RegisterCapabilitiesEvent)}
 * methods (kept upstream-shaped so those ~40 methods port unchanged). Posted once on the bridge bus from mod init
 * after the Registrate flush. Registrations into {@link Capabilities}' item/fluid BLOCK
 * lookups are mirrored (wrapped) into Fabric's {@link ItemStorage#SIDED}/{@link FluidStorage#SIDED} so the
 * rest of the Fabric ecosystem sees Create's inventories and tanks.
 */
public class RegisterCapabilitiesEvent extends com.simibubi.create.foundation.event.bus.Event {

	@SuppressWarnings("unchecked")
	public <T, C, BE extends BlockEntity> void registerBlockEntity(BlockApiLookup<T, C> capability, BlockEntityType<BE> type,
		BiFunction<? super BE, C, @Nullable T> provider) {
		capability.registerForBlockEntity(provider, type);

		if (capability == Capabilities.ItemHandler.BLOCK) {
			BiFunction<? super BE, @Nullable Direction, @Nullable IItemHandler> p = (BiFunction<? super BE, Direction, IItemHandler>) provider;
			ItemStorage.SIDED.registerForBlockEntity((be, side) -> {
				IItemHandler handler = p.apply(be, side);
				return handler == null ? null : Capabilities.toStorage(handler);
			}, type);
		} else if (capability == Capabilities.FluidHandler.BLOCK) {
			BiFunction<? super BE, @Nullable Direction, @Nullable IFluidHandler> p = (BiFunction<? super BE, Direction, IFluidHandler>) provider;
			FluidStorage.SIDED.registerForBlockEntity((be, side) -> {
				IFluidHandler handler = p.apply(be, side);
				return handler == null ? null : Capabilities.toStorage(handler);
			}, type);
		}
	}

	public <T, C> void registerItem(ItemCapability<T, C> capability, ItemCapabilityProvider<T, C> provider, Item... items) {
		capability.registerForItems(provider::getCapability, items);
	}

	@FunctionalInterface
	public interface ItemCapabilityProvider<T, C> {
		@Nullable
		T getCapability(ItemStack stack, C context);
	}
}
