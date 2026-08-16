package com.simibubi.create.foundation.mixin.hooks.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.ItemExtension;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.BlockHitResult;

/** Client half of NeoForge's {@code doesSneakBypassUse} (see ServerPlayerGameModeHooksMixin). */
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeHooksMixin {
	@WrapOperation(method = "performUseItemOn", at = { @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0),
		@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1) })
	private boolean create$sneakBypassUse(ItemStack stack, Operation<Boolean> original, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
		if (original.call(stack))
			return true;
		return ExtensionOverrides.overrides(stack.getItem(), ItemExtension.class, "doesSneakBypassUse", ItemStack.class, LevelReader.class, BlockPos.class, Player.class)
			&& stack.getItem().doesSneakBypassUse(stack, player.level(), hitResult.getBlockPos(), player);
	}
}
