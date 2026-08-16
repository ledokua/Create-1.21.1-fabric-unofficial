package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.extension.ExtensionOverrides;
import com.simibubi.create.foundation.extension.IBlockExtension;
import com.simibubi.create.foundation.extension.ItemExtension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * NeoForge's block-breaking / block-use hooks on the server:
 * {@code canHarvestBlock} (copycats defer to their material), {@code onDestroyedByPlayer} (factory panels remove a
 * sub-panel first) and {@code doesSneakBypassUse} (extendo grip lets sneak-use reach blocks).
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeHooksMixin {

	@Shadow
	@Final
	protected ServerPlayer player;

	@Shadow
	protected ServerLevel level;

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean create$canHarvestBlock(ServerPlayer player, BlockState state, Operation<Boolean> original, BlockPos pos) {
		if (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "canHarvestBlock", BlockState.class, BlockGetter.class, BlockPos.class, Player.class))
			return state.getBlock().canHarvestBlock(state, level, pos, player);
		return original.call(player, state);
	}

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState create$skipPlayerWillDestroy(Block block, Level level, BlockPos pos, BlockState state, Player player, Operation<BlockState> original) {
		// onDestroyedByPlayer's default implementation calls playerWillDestroy itself
		if (create$overridesDestroyed(block))
			return state;
		return original.call(block, level, pos, state, player);
	}

	@WrapOperation(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
	private boolean create$onDestroyedByPlayer(ServerLevel level, BlockPos pos, boolean moving, Operation<Boolean> original, BlockPos argPos, @Local(ordinal = 0) BlockState state) {
		if (!create$overridesDestroyed(state.getBlock()))
			return original.call(level, pos, moving);
		// NeoForge computes canHarvest before removal (vanilla checks the tool afterwards)
		boolean canHarvest = !player.isCreative() && (ExtensionOverrides.overrides(state.getBlock(), IBlockExtension.class, "canHarvestBlock", BlockState.class, BlockGetter.class, BlockPos.class, Player.class)
			? state.getBlock().canHarvestBlock(state, level, pos, player) : player.hasCorrectToolForDrops(state));
		return state.getBlock().onDestroyedByPlayer(state, level, pos, player, canHarvest, level.getFluidState(pos));
	}

	private static boolean create$overridesDestroyed(Block block) {
		return ExtensionOverrides.overrides(block, IBlockExtension.class, "onDestroyedByPlayer", BlockState.class, Level.class, BlockPos.class, Player.class, boolean.class, FluidState.class);
	}

	@WrapOperation(method = "useItemOn", at = { @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0),
		@At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1) })
	private boolean create$sneakBypassUse(ItemStack stack, Operation<Boolean> original, ServerPlayer player, Level level, ItemStack heldStack,
		net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
		// vanilla: flag = !main.isEmpty() || !off.isEmpty(); NeoForge: flag = !main.doesSneakBypassUse() || !off.doesSneakBypassUse()
		if (original.call(stack))
			return true;
		return ExtensionOverrides.overrides(stack.getItem(), ItemExtension.class, "doesSneakBypassUse", ItemStack.class, LevelReader.class, BlockPos.class, Player.class)
			&& stack.getItem().doesSneakBypassUse(stack, level, hitResult.getBlockPos(), player);
	}
}
