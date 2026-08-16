package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.ItemExtension;
import com.simibubi.create.foundation.platform.CommonHooks;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * NeoForge's item-provided enchantments ({@code Item#getEnchantmentLevel} / {@code getAllEnchantments}): the diving
 * helmet has built-in Aqua Affinity. Vanilla reads the ENCHANTMENTS component directly in EnchantmentHelper.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperHooksMixin {

	@Inject(method = "getItemEnchantmentLevel", at = @At("HEAD"), cancellable = true)
	private static void create$itemEnchantmentLevel(Holder<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
		if (ExtensionOverrides.overrides(stack.getItem(), ItemExtension.class, "getEnchantmentLevel", ItemStack.class, Holder.class))
			cir.setReturnValue(stack.getItem().getEnchantmentLevel(stack, enchantment));
	}

	@WrapOperation(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentVisitor;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private static Object create$allEnchantments(ItemStack stack, DataComponentType<?> type, Object fallback, Operation<Object> original) {
		return create$resolve(stack, original.call(stack, type, fallback), null);
	}

	@WrapOperation(method = "runIterationOnItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getOrDefault(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private static Object create$allEnchantmentsInSlot(ItemStack stack, DataComponentType<?> type, Object fallback, Operation<Object> original,
		ItemStack argStack, EquipmentSlot slot, LivingEntity entity) {
		return create$resolve(stack, original.call(stack, type, fallback), entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
	}

	private static Object create$resolve(ItemStack stack, Object vanilla, HolderLookup.RegistryLookup<Enchantment> lookup) {
		if (!(vanilla instanceof ItemEnchantments) || !ExtensionOverrides.overrides(stack.getItem(), ItemExtension.class, "getAllEnchantments", ItemStack.class, HolderLookup.RegistryLookup.class))
			return vanilla;
		if (lookup == null)
			lookup = CommonHooks.resolveLookup(Registries.ENCHANTMENT);
		if (lookup == null)
			return vanilla;
		return stack.getItem().getAllEnchantments(stack, lookup);
	}
}
