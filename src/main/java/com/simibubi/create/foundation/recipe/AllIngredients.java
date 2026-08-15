package com.simibubi.create.foundation.recipe;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.simibubi.create.foundation.event.bus.IEventBus;

/** Upstream registers custom ingredient types here (currently none). Fabric equivalent would be CustomIngredientSerializer.register. */
public class AllIngredients {

	@Internal
	public static void register(IEventBus modEventBus) {}
}
