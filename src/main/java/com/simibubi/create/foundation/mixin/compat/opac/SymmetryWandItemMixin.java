package com.simibubi.create.foundation.mixin.compat.opac;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import xaero.pac.common.server.core.ServerCore;

/**
 * Open Parties and Claims, as its NeoForge build does: the mirrored block breaks of the symmetry wand (OPAC's shared
 * mixin covers the mirrored placements and collects the positions).
 */
@Mixin(value = SymmetryWandItem.class, priority = 1000001)
public abstract class SymmetryWandItemMixin {
	@Inject(method = "remove", remap = false, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
		target = "Lcom/simibubi/create/content/equipment/symmetryWand/mirror/SymmetryMirror;process(Ljava/util/Map;)V"))
	private static void create$opacRemove(Level world, ItemStack wand, Player player, BlockPos pos, CallbackInfo ci) {
		ServerCore.onCreateModSymmetryProcessed(world, player, false);
	}
}
