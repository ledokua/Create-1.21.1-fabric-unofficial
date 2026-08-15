package com.simibubi.create.foundation.client.extensions;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * Registry behind {@code IClient*Extensions.of(...)}. Filled at client init by walking Create's registered
 * items/blocks/fluids and calling their {@code initializeClient} (interface-injected — see
 * {@code foundation/mixin/extension}); upstream gets the same from NeoForge's item/block extension points.
 * The Fabric-side hookups that *consume* these (BuiltinItemRendererRegistry, arm-pose/armor mixins,
 * break-particle mixins) are Phase 3.
 */
public final class ClientExtensionsManager {

	static final Map<Item, IClientItemExtensions> ITEM_EXTENSIONS = new IdentityHashMap<>();
	static final Map<Block, IClientBlockExtensions> BLOCK_EXTENSIONS = new IdentityHashMap<>();
	static final Map<Fluid, IClientFluidTypeExtensions> FLUID_EXTENSIONS = new IdentityHashMap<>();

	private ClientExtensionsManager() {}

	public static void register(Item item, IClientItemExtensions ext) {
		ITEM_EXTENSIONS.put(item, ext);
	}

	public static void register(Block block, IClientBlockExtensions ext) {
		BLOCK_EXTENSIONS.put(block, ext);
	}

	public static void register(Fluid fluid, IClientFluidTypeExtensions ext) {
		FLUID_EXTENSIONS.put(fluid, ext);
	}
}
