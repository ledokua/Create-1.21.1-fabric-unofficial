package com.simibubi.create.foundation.client.extensions;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

/** Client-side block hooks (Create-owned; was NeoForge's). Registered via {@code Block#initializeClient}. */
public interface IClientBlockExtensions {

	IClientBlockExtensions DEFAULT = new IClientBlockExtensions() {};

	static IClientBlockExtensions of(BlockState state) {
		return of(state.getBlock());
	}

	static IClientBlockExtensions of(Block block) {
		return ClientExtensionsManager.BLOCK_EXTENSIONS.getOrDefault(block, DEFAULT);
	}

	/** Return true to suppress vanilla's hit (crack) particles. */
	default boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
		return false;
	}

	/** Return true to suppress vanilla's break particles. */
	default boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
		return false;
	}

	default boolean areBreakingParticlesTinted(BlockState state, net.minecraft.client.multiplayer.ClientLevel level, BlockPos pos) {
		return true;
	}
}
