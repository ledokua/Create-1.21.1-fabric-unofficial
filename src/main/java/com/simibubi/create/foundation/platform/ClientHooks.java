package com.simibubi.create.foundation.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Client-side static helpers upstream takes from NeoForge. */
@Environment(EnvType.CLIENT)
public final class ClientHooks {

	private ClientHooks() {}

	/** Applies the model's item transform for the given context (NeoForge routes this through the model). */
	public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext cameraTransformType, boolean applyLeftHandTransform) {
		return model.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
	}

	/** Vanilla's tooltip-component gathering, exposed as a static (upstream's helper builds tooltip lists this way). */
	public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements,
		Optional<TooltipComponent> itemComponent, int mouseX, int screenWidth, int screenHeight, Font font) {
		List<ClientTooltipComponent> components = new ArrayList<>();
		for (FormattedText text : textElements)
			components.add(ClientTooltipComponent.create(net.minecraft.locale.Language.getInstance().getVisualOrder(text)));
		itemComponent.ifPresent(component -> {
			ClientTooltipComponent client = TooltipComponentCallback.EVENT.invoker().getComponent(component);
			components.add(components.isEmpty() ? 0 : 1, client != null ? client : ClientTooltipComponent.create(component));
		});
		return components;
	}

	/**
	 * Re-mesh the chunk section containing pos (used for BlockEntity#requestModelDataUpdate). Only for the client's own
	 * level: block entities in a contraption's or Ponder's virtual level are drawn by their own renderers, and those are
	 * often set up on Flywheel worker threads, where Sodium throws on any world-renderer call. That exception used to
	 * abort the contraption's visual, so contraptions with copycats, belts, tracks, ... stayed invisible until F3+A.
	 */
	public static void markBlockForRerender(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos) {
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		if (level != mc.level)
			return;
		if (!mc.isSameThread()) {
			net.minecraft.core.BlockPos immutable = pos.immutable();
			mc.execute(() -> markBlockForRerender(level, immutable));
			return;
		}
		if (mc.levelRenderer != null)
			mc.levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
	}
}
