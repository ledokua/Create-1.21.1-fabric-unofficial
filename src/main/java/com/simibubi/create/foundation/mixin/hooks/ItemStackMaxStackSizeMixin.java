package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.ItemExtension;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** NeoForge's {@code Item#getMaxStackSize(ItemStack)} (packages, chromatic compound). Re-entrancy: the extension default calls back into ItemStack. */
@Mixin(ItemStack.class)
public abstract class ItemStackMaxStackSizeMixin {
	@Shadow
	public abstract Item getItem();

	private static final ThreadLocal<Boolean> CREATE$REENTRANT = ThreadLocal.withInitial(() -> false);

	@Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
	private void create$maxStackSize(CallbackInfoReturnable<Integer> cir) {
		Item item = getItem();
		if (CREATE$REENTRANT.get() || !ExtensionOverrides.overrides(item, ItemExtension.class, "getMaxStackSize", ItemStack.class))
			return;
		CREATE$REENTRANT.set(true);
		try {
			cir.setReturnValue(item.getMaxStackSize((ItemStack) (Object) this));
		} finally {
			CREATE$REENTRANT.set(false);
		}
	}
}
