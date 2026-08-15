package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.tick.EntityTickEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/** Posts {@link EntityTickEvent.Pre} where NeoForge does (per entity/passenger tick). Post is unused by Create. */
@Mixin(ServerLevel.class)
public class EntityTickServerLevelMixin {
	@Inject(method = "tickNonPassenger", at = @At("HEAD"))
	private void create$preTick(Entity entity, CallbackInfo ci) {
		CreateEventBus.BUS.post(new EntityTickEvent.Pre(entity));
	}

	@Inject(method = "tickPassenger", at = @At("HEAD"))
	private void create$prePassengerTick(Entity vehicle, Entity passenger, CallbackInfo ci) {
		CreateEventBus.BUS.post(new EntityTickEvent.Pre(passenger));
	}
}
