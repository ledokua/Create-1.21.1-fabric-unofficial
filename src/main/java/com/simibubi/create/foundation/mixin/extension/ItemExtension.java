package com.simibubi.create.foundation.mixin.extension;

import java.util.function.Consumer;

import com.simibubi.create.foundation.client.extensions.IClientItemExtensions;

/** Injected onto {@code Item}: NeoForge's client-extension registration hook Create's items override. */
public interface ItemExtension {
	default void initializeClient(Consumer<IClientItemExtensions> consumer) {}

	/** NeoForge's entity-aware food lookup; the stack's food component. */
	@org.jetbrains.annotations.Nullable
	default net.minecraft.world.food.FoodProperties getFoodProperties(net.minecraft.world.item.ItemStack stack, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity entity) {
		return stack.get(net.minecraft.core.component.DataComponents.FOOD);
	}

	/** NeoForge's pre-block-interaction item hook; vanilla has no such phase, default PASS. */
	default net.minecraft.world.InteractionResult onItemUseFirst(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.context.UseOnContext context) {
		return net.minecraft.world.InteractionResult.PASS;
	}
}
