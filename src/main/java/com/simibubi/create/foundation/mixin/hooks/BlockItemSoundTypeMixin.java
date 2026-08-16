package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's {@code getSoundType(state, level, pos, entity)} for placement sounds ({@code BlockItem#place} uses
 * {@code state.getSoundType()} for volume/pitch and {@code getPlaceSound(state)} for the event).
 */
@Mixin(BlockItem.class)
public class BlockItemSoundTypeMixin {

	private static boolean create$overridesSoundType(BlockState state) {
		return ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class);
	}

	@WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
	private SoundType create$placeSoundType(BlockState state, Operation<SoundType> original, BlockPlaceContext context) {
		if (create$overridesSoundType(state))
			return state.getBlock().getSoundType(state, context.getLevel(), context.getClickedPos(), context.getPlayer());
		return original.call(state);
	}

	@WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/sounds/SoundEvent;"))
	private SoundEvent create$placeSound(BlockItem item, BlockState state, Operation<SoundEvent> original, BlockPlaceContext context) {
		if (create$overridesSoundType(state))
			return state.getBlock().getSoundType(state, context.getLevel(), context.getClickedPos(), context.getPlayer()).getPlaceSound();
		return original.call(item, state);
	}
}
