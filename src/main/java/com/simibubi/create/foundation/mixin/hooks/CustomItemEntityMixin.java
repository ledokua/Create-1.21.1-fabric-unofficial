package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * NeoForge's {@code Item#hasCustomEntity/createEntity}: when an ItemEntity for such an item joins the level it is
 * replaced by the item's own entity (packages become PackageEntities). NeoForge does this from EntityJoinLevelEvent;
 * here at the {@code Level#addFreshEntity} entry point (server side).
 */
@Mixin(Level.class)
public abstract class CustomItemEntityMixin {
	@Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true)
	private void create$customItemEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		Level self = (Level) (Object) this;
		if (self.isClientSide || !(entity instanceof ItemEntity itemEntity))
			return;
		ItemStack stack = itemEntity.getItem();
		if (stack.isEmpty() || !stack.getItem().hasCustomEntity(stack))
			return;
		Entity replacement = stack.getItem().createEntity(self, itemEntity, stack);
		if (replacement == null || replacement == itemEntity)
			return;
		itemEntity.discard();
		cir.setReturnValue(self.addFreshEntity(replacement));
	}
}
