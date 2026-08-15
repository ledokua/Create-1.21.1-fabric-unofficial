package com.tterrag.registrate.fabric;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.tterrag.registrate.builders.MenuBuilder.ScreenFactory;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Client-only registration hookups the builders route through. Upstream Registrate does these via NeoForge
 * client events ({@code EntityRenderersEvent}, {@code RegisterColorHandlersEvent}, {@code
 * RegisterMenuScreensEvent}, ...); Fabric API exposes them as plain registries callable any time after
 * the object exists, so builders just call these from an {@code onRegister} callback.
 * <p>
 * Kept in its own class, only ever reached behind an {@link EnvType#CLIENT} check
 * ({@link RegistrateEnv#runOnClient}), so a dedicated server never loads any of these client classes.
 */
@Environment(EnvType.CLIENT)
public final class RegistrateClientHooks {

	private RegistrateClientHooks() {}

	public static <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<T> type,
		NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer) {
		BlockEntityRenderers.register(type, ctx -> renderer.get().apply(ctx));
	}

	public static <T extends Entity> void registerEntityRenderer(EntityType<T> type,
		NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer) {
		EntityRendererRegistry.register(type, ctx -> renderer.get().apply(ctx));
	}

	public static void registerBlockColor(Block block, NonNullSupplier<Supplier<BlockColor>> color) {
		ColorProviderRegistry.BLOCK.register(color.get().get(), block);
	}

	public static void registerItemColor(Item item, NonNullSupplier<Supplier<ItemColor>> color) {
		ColorProviderRegistry.ITEM.register(color.get().get(), item);
	}

	public static void registerRenderLayer(Block block, Supplier<Supplier<RenderType>> layer) {
		BlockRenderLayerMap.INSTANCE.putBlock(block, layer.get().get());
	}

	public static <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> void registerScreen(MenuType<T> type,
		NonNullSupplier<ScreenFactory<T, SC>> screenFactory) {
		MenuScreens.<T, SC>register(type, (menu, inv, title) -> screenFactory.get().create(menu, inv, title));
	}

	/** Fluid textures/tint via Fabric's fluid rendering API, plus an optional render layer. */
	public static void registerFluidRendering(net.minecraft.world.level.material.Fluid fluid,
		com.simibubi.create.foundation.client.extensions.IClientFluidTypeExtensions ext, @org.jetbrains.annotations.Nullable Supplier<RenderType> layer) {
		net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry.INSTANCE.register(fluid,
			new net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler(ext.getStillTexture(), ext.getFlowingTexture(), ext.getOverlayTexture(), ext.getTintColor()) {
				@Override
				public int getFluidColor(@org.jetbrains.annotations.Nullable net.minecraft.world.level.BlockAndTintGetter view, @org.jetbrains.annotations.Nullable net.minecraft.core.BlockPos pos, net.minecraft.world.level.material.FluidState state) {
					return view == null || pos == null ? ext.getTintColor() : ext.getTintColor(state, view, pos);
				}
			});
		if (layer != null)
			BlockRenderLayerMap.INSTANCE.putFluid(fluid, layer.get());
	}
}
