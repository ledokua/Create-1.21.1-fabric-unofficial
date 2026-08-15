package com.simibubi.create.foundation.mixin.event.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.level.LevelEvent;
import com.simibubi.create.foundation.event.tick.EntityTickEvent;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

@Mixin(ClientLevel.class)
public class EntityTickClientLevelMixin {
	@Inject(method = "tickNonPassenger", at = @At("HEAD"))
	private void create$preTick(Entity entity, CallbackInfo ci) {
		CreateEventBus.BUS.post(new EntityTickEvent.Pre(entity));
	}

	@Inject(method = "tickPassenger", at = @At("HEAD"))
	private void create$prePassengerTick(Entity vehicle, Entity passenger, CallbackInfo ci) {
		CreateEventBus.BUS.post(new EntityTickEvent.Pre(passenger));
	}

	/** NeoForge posts {@code LevelEvent.Load} from the ClientLevel constructor. */
	@Inject(method = "<init>", at = @At("RETURN"))
	private void create$onLoad(CallbackInfo ci) {
		CreateEventBus.BUS.post(new LevelEvent.Load((ClientLevel) (Object) this));
	}
}
