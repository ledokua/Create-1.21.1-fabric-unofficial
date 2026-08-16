package com.simibubi.create.foundation.mixin.hooks.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.event.client.RegisterItemDecorationsEvent;
import com.simibubi.create.foundation.event.client.RegisterItemDecorationsEvent.IItemDecorator;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/** NeoForge's item decorators ({@code ItemDecoratorHandler}), run after vanilla's count/durability/cooldown overlays. */
@Mixin(GuiGraphics.class)
public class ItemDecoratorsMixin {
	@Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
	private void create$renderItemDecorators(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
		if (stack.isEmpty())
			return;
		List<IItemDecorator> decorators = RegisterItemDecorationsEvent.decoratorsFor(stack.getItem());
		if (decorators.isEmpty())
			return;
		create$resetRenderState();
		for (IItemDecorator decorator : decorators)
			if (decorator.render((GuiGraphics) (Object) this, font, stack, x, y))
				create$resetRenderState();
	}

	private static void create$resetRenderState() {
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}
}
