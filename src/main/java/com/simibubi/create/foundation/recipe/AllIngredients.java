package com.simibubi.create.foundation.recipe;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.foundation.event.bus.IEventBus;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

/** Create's custom ingredient types (Fabric {@code CustomIngredientSerializer}s). */
public class AllIngredients {

	@Internal
	public static void register(IEventBus modEventBus) {
		CustomIngredientSerializer.register(BlockTagIngredient.SERIALIZER);
	}
}
