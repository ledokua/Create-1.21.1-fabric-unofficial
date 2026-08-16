package com.simibubi.create.foundation.mixin.hooks.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.EntityExtension;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/** NeoForge's pick-block hooks: {@code getCloneItemStack(state, target, level, pos, player)} and {@code Entity#getPickedResult}. */
@Mixin(Minecraft.class)
public class PickBlockHooksMixin {
	@WrapOperation(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack create$cloneItemStack(Block block, LevelReader level, BlockPos pos, BlockState state, Operation<ItemStack> original) {
		Minecraft mc = (Minecraft) (Object) this;
		if (ExtensionOverrides.overrides(block, IBlockExtension.class, "getCloneItemStack", BlockState.class, HitResult.class, LevelReader.class, BlockPos.class, Player.class))
			return block.getCloneItemStack(state, mc.hitResult, level, pos, mc.player);
		return original.call(block, level, pos, state);
	}

	@WrapOperation(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPickResult()Lnet/minecraft/world/item/ItemStack;"))
	private ItemStack create$pickedResult(Entity entity, Operation<ItemStack> original) {
		Minecraft mc = (Minecraft) (Object) this;
		if (ExtensionOverrides.overrides(entity, EntityExtension.class, "getPickedResult", HitResult.class))
			return entity.getPickedResult(mc.hitResult);
		return original.call(entity);
	}
}
