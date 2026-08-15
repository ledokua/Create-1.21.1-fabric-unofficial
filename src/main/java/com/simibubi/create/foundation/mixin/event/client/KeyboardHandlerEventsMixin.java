package com.simibubi.create.foundation.mixin.event.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.InputEvent;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerEventsMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	/** NeoForge fires InputEvent.Key after vanilla handled the key press (only when no screen is open). */
	@Inject(method = "keyPress", at = @At("TAIL"))
	private void create$onKey(long window, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
		if (window != minecraft.getWindow().getWindow() || minecraft.screen != null)
			return;
		CreateEventBus.BUS.post(new InputEvent.Key(key, scanCode, action, modifiers));
	}
}
