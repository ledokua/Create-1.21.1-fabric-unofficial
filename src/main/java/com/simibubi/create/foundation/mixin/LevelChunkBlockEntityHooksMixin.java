package com.simibubi.create.foundation.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** NeoForge's {@code BlockEntity#onLoad} / {@code #onChunkUnloaded} hooks (see BlockEntityExtension). */
@Mixin(LevelChunk.class)
public class LevelChunkBlockEntityHooksMixin {
	@Shadow
	@Final
	private Map<BlockPos, BlockEntity> blockEntities;

	@Inject(method = "addAndRegisterBlockEntity", at = @At("TAIL"))
	private void create$onLoad(BlockEntity blockEntity, CallbackInfo ci) {
		blockEntity.onLoad();
	}

	@Inject(method = "clearAllBlockEntities", at = @At("HEAD"))
	private void create$onChunkUnloaded(CallbackInfo ci) {
		blockEntities.values().forEach(BlockEntity::onChunkUnloaded);
	}
}
