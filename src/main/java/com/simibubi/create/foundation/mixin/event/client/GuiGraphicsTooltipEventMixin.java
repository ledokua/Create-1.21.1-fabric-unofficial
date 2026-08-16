package com.simibubi.create.foundation.mixin.event.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.RenderTooltipEvent;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsTooltipEventMixin {
	/** NeoForge tracks the stack a tooltip is rendered for in {@code tooltipStack}; vanilla doesn't, so we capture it here. */
	@Unique
	private ItemStack create$tooltipStack = ItemStack.EMPTY;

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("HEAD"))
	private void create$captureStack(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
		create$tooltipStack = stack;
	}

	@Inject(method = "renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
	private void create$releaseStack(Font font, ItemStack stack, int mouseX, int mouseY, CallbackInfo ci) {
		create$tooltipStack = ItemStack.EMPTY;
	}

	@Shadow
	public abstract int guiWidth();

	@Shadow
	public abstract int guiHeight();

	@Inject(method = "renderTooltipInternal", at = @At("HEAD"), cancellable = true)
	private void create$preTooltip(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner, CallbackInfo ci) {
		if (components.isEmpty())
			return;
		RenderTooltipEvent.Pre event = CreateEventBus.BUS.post(new RenderTooltipEvent.Pre(create$tooltipStack, (GuiGraphics) (Object) this, mouseX, mouseY,
			guiWidth(), guiHeight(), font, components, positioner));
		if (event.isCanceled())
			ci.cancel();
	}
}
