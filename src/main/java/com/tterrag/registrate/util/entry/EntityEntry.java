package com.tterrag.registrate.util.entry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityEntry<T extends Entity> extends RegistryEntry<EntityType<?>, EntityType<T>> {

	public static <T extends Entity> EntityEntry<T> unresolved(ResourceLocation id) {
		return new EntityEntry<>(id);
	}

	protected EntityEntry(ResourceLocation id) {
		super(Registries.ENTITY_TYPE, id);
	}

	public boolean is(Entity entity) {
		return entity != null && entity.getType() == get();
	}
}
