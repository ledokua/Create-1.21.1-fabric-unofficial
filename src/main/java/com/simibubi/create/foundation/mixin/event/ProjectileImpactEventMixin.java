package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.ProjectileImpactEvent;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;

@Mixin(Projectile.class)
public class ProjectileImpactEventMixin {
	@Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true)
	private void create$onImpact(HitResult hitResult, CallbackInfoReturnable<ProjectileDeflection> cir) {
		if (CreateEventBus.BUS.post(new ProjectileImpactEvent((Projectile) (Object) this, hitResult)).isCanceled())
			cir.setReturnValue(ProjectileDeflection.NONE);
	}
}
