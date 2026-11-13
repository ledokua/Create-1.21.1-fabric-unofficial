package com.simibubi.create.foundation.mixin.event.client;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.InputEvent;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerEventsMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "keyPress", at = @At("HEAD"))
	private void create$rememberScreen(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci,
		@Share("screenOpen") LocalBooleanRef screenOpen) {
		screenOpen.set(minecraft.screen != null);
	}

	/**
	 * NeoForge fires InputEvent.Key after vanilla handled the key (only when no screen is open). At every RETURN, not
	 * TAIL: the compiled method ends the key-release branch with its own return, so TAIL never saw releases (the
	 * schematic tool menu stayed focused after releasing its key).
	 */
	@Inject(method = "keyPress", at = @At("RETURN"))
	private void create$onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci,
		@Share("screenOpen") LocalBooleanRef screenOpen) {
		if (window != minecraft.getWindow().getWindow() || screenOpen.get() || minecraft.screen != null)
			return;
		// vanilla returns before NeoForge's hook after toggling fullscreen or taking a screenshot
		if (action == GLFW.GLFW_PRESS
			&& (minecraft.options.keyFullscreen.matches(key, scanCode) || minecraft.options.keyScreenshot.matches(key, scanCode)))
			return;
		CreateEventBus.BUS.post(new InputEvent.Key(key, scanCode, action, modifiers));
	}
}
