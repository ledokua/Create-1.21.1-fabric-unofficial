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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;

/** State-side counterparts of {@link IBlockExtension}, injected onto {@code BlockBehaviour.BlockStateBase}. */
public interface IBlockStateExtension {

	private BlockState self() {
		return (BlockState) this;
	}

	private IBlockExtension block() {
		return (IBlockExtension) self().getBlock();
	}

	default SoundType getSoundType(LevelReader level, BlockPos pos, @Nullable Entity entity) {
		return block().getSoundType(self(), level, pos, entity);
	}

	default float getFriction(LevelReader level, BlockPos pos, @Nullable Entity entity) {
		return block().getFriction(self(), level, pos, entity);
	}

	default int getLightEmission(BlockGetter level, BlockPos pos) {
		return block().getLightEmission(self(), level, pos);
	}

	default boolean canHarvestBlock(BlockGetter level, BlockPos pos, Player player) {
		return block().canHarvestBlock(self(), level, pos, player);
	}

	default float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
		return block().getExplosionResistance(self(), level, pos, explosion);
	}

	default ItemStack getCloneItemStack(HitResult target, LevelReader level, BlockPos pos, Player player) {
		return block().getCloneItemStack(self(), target, level, pos, player);
	}

	default boolean addLandingEffects(ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return block().addLandingEffects(self(), level, pos, state2, entity, numberOfParticles);
	}

	default boolean addRunningEffects(Level level, BlockPos pos, Entity entity) {
		return block().addRunningEffects(self(), level, pos, entity);
	}

	default float getEnchantPowerBonus(LevelReader level, BlockPos pos) {
		return block().getEnchantPowerBonus(self(), level, pos);
	}

	default boolean canEntityDestroy(BlockGetter level, BlockPos pos, Entity entity) {
		return block().canEntityDestroy(self(), level, pos, entity);
	}

	default boolean canConnectRedstone(BlockGetter level, BlockPos pos, @Nullable Direction direction) {
		return block().canConnectRedstone(self(), level, pos, direction);
	}

	default boolean isFlammable(BlockGetter level, BlockPos pos, Direction direction) {
		return block().isFlammable(self(), level, pos, direction);
	}

	default int getFlammability(BlockGetter level, BlockPos pos, Direction direction) {
		return block().getFlammability(self(), level, pos, direction);
	}

	default int getFireSpreadSpeed(BlockGetter level, BlockPos pos, Direction direction) {
		return block().getFireSpreadSpeed(self(), level, pos, direction);
	}

	default boolean isScaffolding(LevelReader level, BlockPos pos, LivingEntity entity) {
		return block().isScaffolding(self(), level, pos, entity);
	}

	@Nullable
	default PathType getBlockPathType(BlockGetter level, BlockPos pos, @Nullable Mob mob) {
		return block().getBlockPathType(self(), level, pos, mob);
	}

	default void onNeighborChange(LevelReader level, BlockPos pos, BlockPos neighbor) {
		block().onNeighborChange(self(), level, pos, neighbor);
	}

	default boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState neighborState, Direction dir) {
		return block().hidesNeighborFace(level, pos, self(), neighborState, dir);
	}

	default boolean supportsExternalFaceHiding() {
		return block().supportsExternalFaceHiding(self());
	}

	default boolean getWeakChanges(LevelReader level, BlockPos pos) {
		return block().getWeakChanges(self(), level, pos);
	}

	default boolean onDestroyedByPlayer(Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		return block().onDestroyedByPlayer(self(), level, pos, player, willHarvest, fluid);
	}

	@Nullable
	default BlockState getToolModifiedState(UseOnContext context, ItemAbility itemAbility, boolean simulate) {
		return block().getToolModifiedState(self(), context, itemAbility, simulate);
	}

	default boolean isStickyBlock() {
		return block().isStickyBlock(self());
	}

	default boolean isSlimeBlock() {
		return block().isSlimeBlock(self());
	}

	default boolean canStickTo(BlockState other) {
		return block().canStickTo(self(), other);
	}

	default boolean hasDynamicLightEmission() {
		return block().hasDynamicLightEmission(self());
	}

	default boolean shouldCheckWeakPower(net.minecraft.world.level.SignalGetter level, BlockPos pos, Direction side) {
		return block().shouldCheckWeakPower(self(), level, pos, side);
	}

	default boolean shouldDisplayFluidOverlay(net.minecraft.world.level.BlockAndTintGetter level, BlockPos pos, FluidState fluidState) {
		return block().shouldDisplayFluidOverlay(self(), level, pos, fluidState);
	}

	/** NeoForge's level-aware rotate; blocks that need position context override on the block side. */
	default BlockState rotate(net.minecraft.world.level.LevelAccessor level, BlockPos pos, net.minecraft.world.level.block.Rotation direction) {
		return self().rotate(direction);
	}
}
