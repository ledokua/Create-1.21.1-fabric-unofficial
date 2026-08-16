package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge's face-hiding hooks in {@code Block#shouldRenderFace}: a neighbour that {@code hidesNeighborFace} culls the
 * face of a block that {@code supportsExternalFaceHiding} (copycats, scaffolding, pipes...).
 */
@Mixin(Block.class)
public class BlockFaceHidingMixin {
	@Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
	private static void create$hideFace(BlockState state, BlockGetter level, BlockPos pos, Direction face, BlockPos neighborPos, CallbackInfoReturnable<Boolean> cir) {
		BlockState neighborState = level.getBlockState(neighborPos);
		if (neighborState.hidesNeighborFace(level, neighborPos, state, face.getOpposite()) && state.supportsExternalFaceHiding())
			cir.setReturnValue(false);
	}
}
