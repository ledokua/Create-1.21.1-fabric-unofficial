package com.simibubi.create.foundation.client.extensions;

import java.util.IdentityHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Registry behind {@code IClient*Extensions.of(...)}. Filled at client init by walking Create's registered
 * items/blocks/fluids and calling their {@code initializeClient} (interface-injected — see
 * {@code foundation/extension}); upstream gets the same from NeoForge's item/block extension points.
 * The Fabric-side hookups that *consume* these (BuiltinItemRendererRegistry, arm-pose/armor mixins,
 * break-particle mixins) are Phase 3.
 */
public final class ClientExtensionsManager {

	// Extensions are registered as suppliers and instantiated on first use: NeoForge creates them lazily too
	// (Item#initializeClient runs during client setup), and some (e.g. CardboardArmorStealthOverlay's Gui) need a
	// fully-constructed Minecraft instance, which the Registrate flush predates.
	private static final Map<Item, Supplier<IClientItemExtensions>> ITEM_SUPPLIERS = new IdentityHashMap<>();
	private static final Map<Block, Supplier<IClientBlockExtensions>> BLOCK_SUPPLIERS = new IdentityHashMap<>();
	static final Map<Item, IClientItemExtensions> ITEM_EXTENSIONS = new IdentityHashMap<>();
	static final Map<Block, IClientBlockExtensions> BLOCK_EXTENSIONS = new IdentityHashMap<>();
	static final Map<Fluid, IClientFluidTypeExtensions> FLUID_EXTENSIONS = new IdentityHashMap<>();

	private ClientExtensionsManager() {}

	public static void register(Item item, Supplier<IClientItemExtensions> ext) {
		ITEM_SUPPLIERS.put(item, ext);
		ITEM_EXTENSIONS.remove(item);
	}

	public static void register(Block block, Supplier<IClientBlockExtensions> ext) {
		BLOCK_SUPPLIERS.put(block, ext);
		BLOCK_EXTENSIONS.remove(block);
	}

	public static void register(Item item, IClientItemExtensions ext) {
		register(item, () -> ext);
	}

	public static void register(Block block, IClientBlockExtensions ext) {
		register(block, () -> ext);
	}

	public static void register(Fluid fluid, IClientFluidTypeExtensions ext) {
		FLUID_EXTENSIONS.put(fluid, ext);
	}

	@Nullable
	static IClientItemExtensions itemExtension(Item item) {
		IClientItemExtensions ext = ITEM_EXTENSIONS.get(item);
		if (ext == null) {
			Supplier<IClientItemExtensions> supplier = ITEM_SUPPLIERS.get(item);
			if (supplier == null)
				return null;
			ext = supplier.get();
			ITEM_EXTENSIONS.put(item, ext);
		}
		return ext;
	}

	@Nullable
	static IClientBlockExtensions blockExtension(Block block) {
		IClientBlockExtensions ext = BLOCK_EXTENSIONS.get(block);
		if (ext == null) {
			Supplier<IClientBlockExtensions> supplier = BLOCK_SUPPLIERS.get(block);
			if (supplier == null)
				return null;
			ext = supplier.get();
			BLOCK_EXTENSIONS.put(block, ext);
		}
		return ext;
	}
}
