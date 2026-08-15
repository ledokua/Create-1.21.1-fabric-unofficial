package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.foundation.mixin.extension.BlockEntityExtension;

import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class BlockEntityExtensionMixin implements BlockEntityExtension {
}
