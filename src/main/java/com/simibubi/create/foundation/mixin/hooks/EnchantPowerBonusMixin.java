package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

/** NeoForge's {@code getEnchantPowerBonus} (copycat bookshelves count as enchanting power providers). */
@Mixin(EnchantingTableBlock.class)
public class EnchantPowerBonusMixin {
	@WrapOperation(method = "isValidBookShelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
	private static boolean create$enchantPowerBonus(BlockState state, TagKey<Block> tag, Operation<Boolean> original, Level level, BlockPos pos, BlockPos offset) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "getEnchantPowerBonus", BlockState.class, LevelReader.class, BlockPos.class))
			return state.getBlock().getEnchantPowerBonus(state, level, pos.offset(offset)) > 0;
		return original.call(state, tag);
	}
}
