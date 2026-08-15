package com.simibubi.create.foundation.mixin.event.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.InputEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerEventsMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	public abstract double xpos();

	@Shadow
	public abstract double ypos();

	@Shadow
	public abstract boolean isLeftPressed();

	@Shadow
	public abstract boolean isMiddlePressed();

	@Shadow
	public abstract boolean isRightPressed();

	@Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
	private void create$onPress(long window, int button, int action, int modifiers, CallbackInfo ci) {
		if (window != minecraft.getWindow().getWindow())
			return;
		if (CreateEventBus.BUS.post(new InputEvent.MouseButton.Pre(button, action, modifiers)).isCanceled())
			ci.cancel();
	}

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void create$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
		if (window != minecraft.getWindow().getWindow())
			return;
		double sensitivity = minecraft.options.mouseWheelSensitivity().get();
		boolean discrete = minecraft.options.discreteMouseScroll().get();
		double dx = (discrete ? Math.signum(xOffset) : xOffset) * sensitivity;
		double dy = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;
		double mouseX = xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
		double mouseY = ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
		if (CreateEventBus.BUS.post(new InputEvent.MouseScrollingEvent(dx, dy, isLeftPressed(), isMiddlePressed(), isRightPressed(), mouseX, mouseY)).isCanceled())
			ci.cancel();
	}
}
