package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** NeoForge's {@code Item#onEntitySwing}: an item can veto the swing animation (zapper, potato cannon). */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {
	@Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
	private void create$onEntitySwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		ItemStack stack = self.getItemInHand(hand);
		if (!stack.isEmpty() && stack.getItem().onEntitySwing(stack, self, hand))
			ci.cancel();
	}
}
