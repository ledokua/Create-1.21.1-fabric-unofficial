package com.simibubi.create.foundation.event.entity;

import com.simibubi.create.foundation.event.bus.Event;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

/** Lifecycle event: register default attributes for living entity types. Backed by Fabric's registry. */
public class EntityAttributeCreationEvent extends Event {

	public void put(EntityType<? extends LivingEntity> entity, AttributeSupplier map) {
		FabricDefaultAttributeRegistry.register(entity, map);
	}
}
