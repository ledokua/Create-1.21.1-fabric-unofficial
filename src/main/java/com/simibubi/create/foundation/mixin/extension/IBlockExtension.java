package com.simibubi.create.foundation.mixin.extension;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.platform.ItemAbility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;

/**
 * NeoForge's context-aware block hooks (Create-owned), interface-injected onto {@code Block} so Create's
 * overrides compile. Defaults delegate to the vanilla context-free methods. <b>Callers:</b> vanilla never
 * consults these; NeoForge patches the call sites. Until Phase 3 adds the corresponding mixins (sound type,
 * friction, light emission, redstone connectivity, ...), overrides only take effect where Create's own code
 * calls them via {@link IBlockStateExtension}.
 */
public interface IBlockExtension {

	private Block self() {
		return (Block) this;
	}

	default SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
		return self().getSoundType(state);
	}

	default float getFriction(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
		return self().getFriction();
	}

	default boolean hasDynamicLightEmission(BlockState state) {
		return false;
	}

	default int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getLightEmission();
	}

	default boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
		return player.hasCorrectToolForDrops(state);
	}

	default float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
		return self().getExplosionResistance();
	}

	default ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
		return self().getCloneItemStack(level, pos, state);
	}

	default boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	default boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
		return false;
	}

	default float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
		return state.is(net.minecraft.tags.BlockTags.ENCHANTMENT_POWER_PROVIDER) ? 1 : 0;
	}

	default boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
		return true;
	}

	default boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
		return state.isSignalSource() && direction != null;
	}

	default boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.ignitedByLava();
	}

	default int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		var entry = net.fabricmc.fabric.api.registry.FlammableBlockRegistry.getDefaultInstance().get(self());
		return entry == null ? 0 : entry.getBurnChance();
	}

	default int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		var entry = net.fabricmc.fabric.api.registry.FlammableBlockRegistry.getDefaultInstance().get(self());
		return entry == null ? 0 : entry.getSpreadChance();
	}

	default boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
		return state.is(net.minecraft.world.level.block.Blocks.SCAFFOLDING);
	}

	@Nullable
	default PathType getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
		return null;
	}

	default void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {}

	default boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
		return false;
	}

	default boolean supportsExternalFaceHiding(BlockState state) {
		return !state.canOcclude();
	}

	default boolean getWeakChanges(BlockState state, LevelReader level, BlockPos pos) {
		return false;
	}

	default boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
		self().playerWillDestroy(level, pos, state, player);
		return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
	}

	@Nullable
	default BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
		return null;
	}

	default boolean isStickyBlock(BlockState state) {
		return state.is(net.minecraft.world.level.block.Blocks.SLIME_BLOCK) || state.is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK);
	}

	default boolean isSlimeBlock(BlockState state) {
		return state.is(net.minecraft.world.level.block.Blocks.SLIME_BLOCK);
	}

	default boolean canStickTo(BlockState state, BlockState other) {
		if (state.is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK) && other.is(net.minecraft.world.level.block.Blocks.SLIME_BLOCK))
			return false;
		if (state.is(net.minecraft.world.level.block.Blocks.SLIME_BLOCK) && other.is(net.minecraft.world.level.block.Blocks.HONEY_BLOCK))
			return false;
		return isStickyBlock(state) || isStickyBlock(other);
	}
}
