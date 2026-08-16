package com.simibubi.create.foundation.mixin.hooks.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.extension.BakedModelExtension;
import com.simibubi.create.foundation.extension.ExtensionOverrides;

import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * NeoForge's {@code BakedModel#applyTransform} (models may apply their own transforms and swap themselves, e.g.
 * goggles rendering as the partial model on the head).
 */
@Mixin(ItemRenderer.class)
public class ItemRendererTransformMixin {
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/ItemTransform;apply(ZLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void create$applyTransform(ItemTransform transform, boolean leftHand, PoseStack poseStack, Operation<Void> original,
		ItemStack stack, ItemDisplayContext context, boolean argLeftHand, PoseStack argPoseStack, @Local(argsOnly = true) LocalRef<BakedModel> model) {
		BakedModel current = model.get();
		if (ExtensionOverrides.overrides(current, BakedModelExtension.class, "applyTransform", ItemDisplayContext.class, PoseStack.class, boolean.class)) {
			BakedModel replacement = current.applyTransform(context, poseStack, leftHand);
			if (replacement != null)
				model.set(replacement);
			return;
		}
		original.call(transform, leftHand, poseStack);
	}
}
