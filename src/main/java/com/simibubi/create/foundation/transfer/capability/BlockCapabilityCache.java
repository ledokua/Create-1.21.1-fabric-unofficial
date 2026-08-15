package com.simibubi.create.foundation.transfer.capability;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * Cached lookup of a capability at a fixed position. Create-owned facade over Fabric's
 * {@link BlockApiCache}, keeping upstream's constructor/accessor shape. Fabric's cache handles block-entity
 * invalidation itself, so upstream's {@code invalidationListener} is only fired when {@link #getCapability()}
 * observes the resolved value changing (null → non-null, non-null → null, or a different instance) — the
 * situations upstream calls it in.
 */
public final class BlockCapabilityCache<T, C> {

	public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
		return create(capability, level, pos, context, () -> true, () -> {});
	}

	public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context,
		BooleanSupplier isValid, Runnable invalidationListener) {
		return new BlockCapabilityCache<>(capability, level, pos, context, isValid, invalidationListener);
	}

	private final BlockApiCache<T, C> cache;
	private final BlockCapability<T, C> capability;
	private final BlockPos pos;
	private final C context;
	private final BooleanSupplier isValid;
	private final Runnable invalidationListener;
	@Nullable
	private T last;
	private boolean resolvedOnce;

	private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
		this.cache = BlockApiCache.create(capability, level, pos.immutable());
		this.capability = capability;
		this.pos = pos.immutable();
		this.context = context;
		this.isValid = isValid;
		this.invalidationListener = invalidationListener;
	}

	public ServerLevel level() {
		return cache.getWorld();
	}

	public BlockPos pos() {
		return pos;
	}

	public C context() {
		return context;
	}

	public BlockCapability<T, C> capability() {
		return capability;
	}

	@Nullable
	public T getCapability() {
		if (!isValid.getAsBoolean())
			return null;
		T now = cache.find(context);
		if (resolvedOnce && now != last)
			invalidationListener.run();
		resolvedOnce = true;
		last = now;
		return now;
	}
}
