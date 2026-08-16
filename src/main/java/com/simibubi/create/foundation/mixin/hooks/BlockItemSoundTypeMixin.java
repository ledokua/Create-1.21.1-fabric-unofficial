package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge's {@code getSoundType(state, level, pos, entity)} for placement sounds ({@code BlockItem#place}). */
@Mixin(BlockItem.class)
public class BlockItemSoundTypeMixin {
	@WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/SoundType;"))
	private SoundType create$placeSound(BlockItem item, BlockState state, Operation<SoundType> original, BlockPlaceContext context) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class))
			return state.getBlock().getSoundType(state, context.getLevel(), context.getClickedPos(), context.getPlayer());
		return original.call(item, state);
	}
}
