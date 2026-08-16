package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * NeoForge routes block-entity sync packets through {@code BlockEntity#onDataPacket} (which Create's SyncedBlockEntity
 * overrides to read client-side data); vanilla calls {@code loadWithComponents} directly inside the
 * {@code handleBlockEntityData} lambda ({@code method_38542}, intermediary name — it has no Mojang name).
 */
@Mixin(ClientPacketListener.class)
public abstract class BlockEntityDataPacketMixin {
	@Shadow
	public abstract Connection getConnection();

	@WrapOperation(method = "method_38542", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;loadWithComponents(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V"), remap = false)
	private void create$onDataPacket(BlockEntity blockEntity, CompoundTag tag, HolderLookup.Provider registries, Operation<Void> original, ClientboundBlockEntityDataPacket packet) {
		blockEntity.onDataPacket(getConnection(), packet, registries);
	}
}
