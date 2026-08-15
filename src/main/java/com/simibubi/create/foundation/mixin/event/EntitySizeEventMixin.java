package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.EntityEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

/** NeoForge's {@code EntityEvent.Size}: fired whenever an entity recomputes its dimensions. */
@Mixin(Entity.class)
public abstract class EntitySizeEventMixin {
	@Shadow
	private EntityDimensions dimensions;

	@Shadow
	public abstract Pose getPose();

	@ModifyExpressionValue(method = "refreshDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
	private EntityDimensions create$onSize(EntityDimensions newSize) {
		EntityEvent.Size event = CreateEventBus.BUS.post(new EntityEvent.Size((Entity) (Object) this, getPose(), dimensions, newSize));
		return event.getNewSize();
	}
}
