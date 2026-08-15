package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.tick.PlayerTickEvent;

import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public class PlayerTickMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void create$preTick(CallbackInfo ci) {
		CreateEventBus.BUS.post(new PlayerTickEvent.Pre((Player) (Object) this));
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void create$postTick(CallbackInfo ci) {
		CreateEventBus.BUS.post(new PlayerTickEvent.Post((Player) (Object) this));
	}
}
