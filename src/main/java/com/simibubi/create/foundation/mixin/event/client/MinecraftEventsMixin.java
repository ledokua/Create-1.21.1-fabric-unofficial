package com.simibubi.create.foundation.mixin.event.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.InputEvent;
import com.simibubi.create.foundation.event.entity.player.PlayerInteractEvent;
import com.simibubi.create.foundation.event.level.LevelEvent;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;

/** Client-level unload, and NeoForge's {@code InputEvent.InteractionKeyMappingTriggered} / {@code LeftClickEmpty}. */
@Mixin(Minecraft.class)
public abstract class MinecraftEventsMixin {
	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	public Options options;

	@Shadow
	private int rightClickDelay;

	// ---- LevelEvent.Unload ----
	@Inject(method = "setLevel", at = @At("HEAD"))
	private void create$onSetLevel(ClientLevel newLevel, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
		if (level != null && level != newLevel)
			CreateEventBus.BUS.post(new LevelEvent.Unload(level));
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
	private void create$onDisconnect(Screen screen, boolean keepResourcePacks, CallbackInfo ci) {
		if (level != null)
			CreateEventBus.BUS.post(new LevelEvent.Unload(level));
	}

	// ---- InteractionKeyMappingTriggered ----
	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void create$onStartAttack(CallbackInfoReturnable<Boolean> cir) {
		if (create$fireInputEvent(0, options.keyAttack, InteractionHand.MAIN_HAND))
			cir.setReturnValue(false);
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void create$onStartUseItem(CallbackInfo ci) {
		if (create$fireInputEvent(1, options.keyUse, InteractionHand.MAIN_HAND)) {
			// NeoForge fires this after startUseItem has set the 4-tick right-click delay. Without it, holding the use
			// key re-fires a handled click every tick (e.g. train controls turned on and straight back off).
			rightClickDelay = 4;
			ci.cancel();
		}
	}

	@Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
	private void create$onPickBlock(CallbackInfo ci) {
		if (create$fireInputEvent(2, options.keyPickItem, InteractionHand.MAIN_HAND))
			ci.cancel();
	}

	private boolean create$fireInputEvent(int button, KeyMapping keyMapping, InteractionHand hand) {
		InputEvent.InteractionKeyMappingTriggered event = CreateEventBus.BUS.post(new InputEvent.InteractionKeyMappingTriggered(button, keyMapping, hand));
		if (event.isCanceled()) {
			if (event.shouldSwingHand() && player != null)
				player.swing(hand);
			return true;
		}
		return false;
	}

	// ---- PlayerInteractEvent.LeftClickEmpty (attack key with nothing under the crosshair) ----
	@Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
	private void create$onLeftClickEmpty(CallbackInfoReturnable<Boolean> cir) {
		if (player != null)
			CreateEventBus.BUS.post(new PlayerInteractEvent.LeftClickEmpty(player));
	}
}
