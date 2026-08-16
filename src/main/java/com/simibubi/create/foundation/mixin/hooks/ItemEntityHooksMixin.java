package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/** NeoForge's {@code Item#onEntityItemUpdate}: an item can take over its ItemEntity's tick (legacy chromatic compound etc.). */
@Mixin(ItemEntity.class)
public abstract class ItemEntityHooksMixin {
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void create$onEntityItemUpdate(CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		ItemStack stack = self.getItem();
		if (!stack.isEmpty() && stack.getItem().onEntityItemUpdate(stack, self))
			ci.cancel();
	}
}
