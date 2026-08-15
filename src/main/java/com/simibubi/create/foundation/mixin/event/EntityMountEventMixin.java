package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.EntityMountEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Mixin(Entity.class)
public abstract class EntityMountEventMixin {
	@Shadow
	public abstract Level level();

	@Shadow
	public abstract Entity getVehicle();

	@Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z"), cancellable = true)
	private void create$onMount(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
		Entity self = (Entity) (Object) this;
		if (CreateEventBus.BUS.post(new EntityMountEvent(self, vehicle, level(), true)).isCanceled())
			cir.setReturnValue(false);
	}

	@Inject(method = "removeVehicle", at = @At("HEAD"), cancellable = true)
	private void create$onDismount(CallbackInfo ci) {
		Entity vehicle = getVehicle();
		if (vehicle == null)
			return;
		Entity self = (Entity) (Object) this;
		if (CreateEventBus.BUS.post(new EntityMountEvent(self, vehicle, level(), false)).isCanceled())
			ci.cancel();
	}
}
