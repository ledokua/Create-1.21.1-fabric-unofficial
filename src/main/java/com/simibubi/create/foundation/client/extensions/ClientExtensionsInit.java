package com.simibubi.create.foundation.client.extensions;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.transfer.fluid.FluidType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/**
 * NeoForge calls {@code Item/Block#initializeClient(Consumer)} and {@code FluidType#initializeClient} once during client
 * setup so objects can hand out their client extensions. We walk the registries at client init and do the same; items
 * whose extension supplies a custom renderer are wired into Fabric's {@link BuiltinItemRendererRegistry} (used for
 * models with the {@code builtin/entity} parent, same trigger as NeoForge's BEWLR path).
 */
@Environment(EnvType.CLIENT)
public final class ClientExtensionsInit {

	private ClientExtensionsInit() {}

	public static void init() {
		int items = 0, blocks = 0, fluids = 0;
		for (Item item : BuiltInRegistries.ITEM) {
			boolean[] found = { false };
			item.initializeClient(ext -> {
				ClientExtensionsManager.register(item, ext);
				found[0] = true;
			});
			if (found[0]) {
				items++;
				BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, poseStack, buffers, light, overlay) -> {
					BlockEntityWithoutLevelRenderer renderer = IClientItemExtensions.of(item).getCustomRenderer();
					if (renderer == null)
						renderer = net.minecraft.client.Minecraft.getInstance().getItemRenderer().blockEntityRenderer;
					renderer.renderByItem(stack, mode, poseStack, buffers, light, overlay);
				});
			}
		}
		for (Block block : BuiltInRegistries.BLOCK) {
			boolean[] found = { false };
			block.initializeClient(ext -> {
				ClientExtensionsManager.register(block, ext);
				found[0] = true;
			});
			if (found[0])
				blocks++;
		}
		for (Fluid fluid : BuiltInRegistries.FLUID) {
			FluidType type = fluid.getFluidType();
			if (type == null)
				continue;
			boolean[] found = { false };
			type.initializeClient(ext -> {
				ClientExtensionsManager.register(fluid, ext);
				found[0] = true;
			});
			if (found[0])
				fluids++;
		}
		Create.LOGGER.debug("Client extensions: {} items, {} blocks, {} fluids", items, blocks, fluids);
	}
}
