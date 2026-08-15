package com.simibubi.create.foundation.mixin.event;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.living.LivingChangeTargetEvent;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public abstract class MobTargetEventMixin {
	@ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
	@Nullable
	private LivingEntity create$changeTarget(@Nullable LivingEntity target) {
		Mob self = (Mob) (Object) this;
		LivingChangeTargetEvent event = CreateEventBus.BUS.post(new LivingChangeTargetEvent(self, target));
		if (event.isCanceled())
			return self.getTarget();
		return event.getNewAboutToBeSetTarget();
	}
}
