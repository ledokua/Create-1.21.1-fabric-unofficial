package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** NeoForge's {@code BlockEntity#onLoad} / {@code #onChunkUnloaded} hooks (see BlockEntityExtension). */
@Mixin(LevelChunk.class)
public class LevelChunkBlockEntityHooksMixin {
	@Inject(method = "addAndRegisterBlockEntity", at = @At("TAIL"))
	private void create$onLoad(BlockEntity blockEntity, CallbackInfo ci) {
		blockEntity.onLoad();
	}

	@Inject(method = "removeBlockEntity", at = @At("HEAD"))
	private void create$removeAuxLight(BlockPos pos, CallbackInfo ci) {
		LevelChunk self = (LevelChunk) (Object) this;
		if (self.getBlockEntity(pos) != null)
			self.getLevel().getAuxLightManager(pos).removeLightAt(pos);
	}

	@Inject(method = "clearAllBlockEntities", at = @At("HEAD"))
	private void create$onChunkUnloaded(CallbackInfo ci) {
		((LevelChunk) (Object) this).getBlockEntities().values().forEach(BlockEntity::onChunkUnloaded);
	}
}
