package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.foundation.event.DropCapture;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

/** Diverts death drops into {@link LivingEntityEventsMixin}'s capture list while a LivingDropsEvent is being assembled. */
@Mixin(Entity.class)
public class EntityDropCaptureMixin {
	@WrapOperation(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean create$captureDrop(Level level, Entity itemEntity, Operation<Boolean> original) {
		if ((Object) this instanceof DropCapture living && living.create$captureDrop((ItemEntity) itemEntity))
			return true;
		return original.call(level, itemEntity);
	}
}
