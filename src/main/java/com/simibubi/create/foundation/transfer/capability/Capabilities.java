package com.simibubi.create.foundation.transfer.capability;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.transfer.capability.bridge.FluidHandlerStorage;
import com.simibubi.create.foundation.transfer.capability.bridge.ItemHandlerStorage;
import com.simibubi.create.foundation.transfer.capability.bridge.StorageFluidHandler;
import com.simibubi.create.foundation.transfer.capability.bridge.StorageFluidHandlerItem;
import com.simibubi.create.foundation.transfer.capability.bridge.StorageItemHandler;
import com.simibubi.create.foundation.transfer.fluid.FluidBucketWrapper;
import com.simibubi.create.foundation.transfer.fluid.IFluidHandler;
import com.simibubi.create.foundation.transfer.fluid.IFluidHandlerItem;
import com.simibubi.create.foundation.transfer.item.IItemHandler;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BucketItem;

/**
 * The item/fluid handler capability lookups Create's code is written against. Create-owned facade (see
 * PORTING_PLAN.md, "NeoForge → Fabric translation strategy") backed by Fabric's real
 * {@link BlockApiLookup}/{@link ItemApiLookup}, bridged one-way to Fabric's Transfer API:
 * <ul>
 * <li>Create's own block entities are registered here by {@link RegisterCapabilitiesEvent} — which also
 * dual-registers them (wrapped) into {@link ItemStorage#SIDED}/{@link FluidStorage#SIDED}, so hoppers,
 * vanilla containers' interactions and other Fabric mods see Create inventories/tanks natively.</li>
 * <li>Anything not registered here falls back to the Fabric lookups (which already cover vanilla containers
 * and every Transfer-API mod), wrapped back into {@link IItemHandler}/{@link IFluidHandler}.</li>
 * </ul>
 * Contexts follow upstream: {@code @Nullable Direction} for blocks, {@code Void} (pass {@code null}) for items.
 * Call sites read {@code Capabilities.ItemHandler.BLOCK.find(level, pos, side)} — Fabric's own method.
 */
public final class Capabilities {

	private Capabilities() {}

	public static final class ItemHandler {
		public static final BlockCapability<IItemHandler, @Nullable Direction> BLOCK = block("item_handler", IItemHandler.class);

		static {
			BLOCK.registerFallback((level, pos, state, be, side) -> {
				Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, state, be, side);
				return storage == null ? null : StorageItemHandler.of(storage);
			});
		}

		private ItemHandler() {}
	}

	public static final class FluidHandler {
		public static final BlockCapability<IFluidHandler, @Nullable Direction> BLOCK = block("fluid_handler", IFluidHandler.class);
		public static final ItemCapability<IFluidHandlerItem, Void> ITEM = item("fluid_handler", IFluidHandlerItem.class);

		static {
			BLOCK.registerFallback((level, pos, state, be, side) -> {
				Storage<FluidVariant> storage = FluidStorage.SIDED.find(level, pos, state, be, side);
				return storage == null ? null : new StorageFluidHandler(storage);
			});
			ITEM.registerFallback((stack, ctx) -> {
				if (stack.getItem() instanceof BucketItem)
					return new FluidBucketWrapper(stack.copy());
				return StorageFluidHandlerItem.of(stack);
			});
		}

		private FluidHandler() {}
	}

	/** Reverse adapters, used by {@link RegisterCapabilitiesEvent} to expose Create handlers to Fabric. */
	static Storage<ItemVariant> toStorage(IItemHandler handler) {
		return handler instanceof StorageItemHandler wrapped ? wrapped.storage() : new ItemHandlerStorage(handler);
	}

	static Storage<FluidVariant> toStorage(IFluidHandler handler) {
		return handler instanceof StorageFluidHandler wrapped ? wrapped.storage() : new FluidHandlerStorage(handler);
	}

	private static <T> BlockCapability<T, @Nullable Direction> block(String name, Class<T> apiClass) {
		BlockApiLookup<T, @Nullable Direction> lookup = BlockApiLookup.get(Create.asResource(name), apiClass, Direction.class);
		return new DelegatingBlockCapability<>(lookup);
	}

	private static <T> ItemCapability<T, Void> item(String name, Class<T> apiClass) {
		ItemApiLookup<T, Void> lookup = ItemApiLookup.get(Create.asResource(name), apiClass, Void.class);
		return new DelegatingItemCapability<>(lookup);
	}
}
