package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * NeoForge routes chunk-packet block-entity data through {@code BlockEntity#handleUpdateTag}; vanilla calls
 * {@code loadWithComponents} inside the {@code replaceWithPacketData} lambda ({@code method_31716}, intermediary name).
 */
@Mixin(LevelChunk.class)
public class LevelChunkUpdateTagMixin {
	@WrapOperation(method = "method_31716", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"), remap = false)
	private void create$handleUpdateTag(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries, Operation<Void> original) {
		blockEntity.handleUpdateTag(tag, registries);
	}
}
