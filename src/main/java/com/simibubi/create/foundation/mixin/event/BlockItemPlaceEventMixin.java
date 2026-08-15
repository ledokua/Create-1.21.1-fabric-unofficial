package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.event.BlockSnapshot;
import com.simibubi.create.foundation.event.EventHooks;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NeoForge posts {@code BlockEvent.EntityPlaceEvent} for every block placed by an item (via captured block
 * snapshots in {@code BlockItem#place}); we snapshot before {@code placeBlock} and post after, restoring on cancel.
 */
@Mixin(BlockItem.class)
public class BlockItemPlaceEventMixin {
	@Unique
	private final ThreadLocal<BlockSnapshot> create$snapshot = new ThreadLocal<>();

	@Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private void create$beforePlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (context.getLevel().isClientSide)
			return;
		create$snapshot.set(BlockSnapshot.create(context.getLevel().dimension(), context.getLevel(), context.getClickedPos()));
	}

	@Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z", shift = At.Shift.AFTER))
	private void create$afterPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		BlockSnapshot snapshot = create$snapshot.get();
		if (snapshot == null)
			return;
		create$snapshot.remove();
		BlockState placed = context.getLevel().getBlockState(context.getClickedPos());
		if (placed == snapshot.getState())
			return;
		if (EventHooks.onBlockPlace(context.getPlayer(), snapshot, context.getClickedFace())) {
			snapshot.restore(true, false);
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
