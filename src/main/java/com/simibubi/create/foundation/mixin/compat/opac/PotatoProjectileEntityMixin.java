package com.simibubi.create.foundation.mixin.compat.opac;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;

import net.minecraft.world.phys.EntityHitResult;

import xaero.pac.common.server.core.ServerCore;

/** Open Parties and Claims, as its old Fabric Create support did: potato cannon shots hitting protected entities. */
@Mixin(PotatoProjectileEntity.class)
public abstract class PotatoProjectileEntityMixin {
	@Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
	private void create$opacHit(EntityHitResult ray, CallbackInfo ci) {
		if (!ServerCore.isProjectileHitAllowed((PotatoProjectileEntity) (Object) this, ray))
			ci.cancel();
	}
}
