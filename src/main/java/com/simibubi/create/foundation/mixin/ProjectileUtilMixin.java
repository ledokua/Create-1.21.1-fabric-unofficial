package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;

/**
 * NeoForge lets projectiles hit entities sharing the shooter's root vehicle when {@code canRiderInteract()}; Create
 * makes that true for anything mounted on a contraption. Vanilla only has the "same root vehicle → skip" check, so we
 * make that comparison fail (null root) when the candidate's root vehicle is a contraption.
 */
@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
	@ModifyExpressionValue(method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRootVehicle()Lnet/minecraft/world/entity/Entity;", ordinal = 0))
	private static Entity create$interactWithEntitiesOnContraptions(Entity rootVehicle) {
		return rootVehicle instanceof AbstractContraptionEntity ? null : rootVehicle;
	}
}
