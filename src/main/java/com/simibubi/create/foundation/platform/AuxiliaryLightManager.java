package com.simibubi.create.foundation.platform;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * NeoForge's auxiliary light storage (lets a block entity report a light level without a block state property, e.g.
 * copycats glowing like their material). Fabric has no equivalent, so this is a per-level in-memory map consulted by
 * {@code BlockLightEmissionMixin} through {@code IBlockExtension#getLightEmission(state, level, pos)}; writers get a
 * light-engine re-check. Values aren't persisted — copycat block entities re-publish theirs in {@code onLoad}.
 */
public final class AuxiliaryLightManager {

	private static final Map<Level, AuxiliaryLightManager> MANAGERS = Collections.synchronizedMap(new WeakHashMap<>());

	/** Manager for non-level block getters (virtual worlds): stores nothing. */
	public static final AuxiliaryLightManager NONE = new AuxiliaryLightManager(null);

	public static AuxiliaryLightManager of(Level level) {
		return MANAGERS.computeIfAbsent(level, AuxiliaryLightManager::new);
	}

	private final Level level;
	private final Long2IntMap lights = Long2IntMaps.synchronize(new Long2IntOpenHashMap());

	private AuxiliaryLightManager(Level level) {
		this.level = level;
	}

	public void setLightAt(BlockPos pos, int value) {
		if (level == null)
			return;
		long key = pos.asLong();
		int previous = value > 0 ? lights.put(key, value) : lights.remove(key);
		if (previous != value)
			level.getChunkSource().getLightEngine().checkBlock(pos);
	}

	public void removeLightAt(BlockPos pos) {
		if (level == null)
			return;
		if (lights.remove(pos.asLong()) != 0)
			level.getChunkSource().getLightEngine().checkBlock(pos);
	}

	public int getLightAt(BlockPos pos) {
		return level == null ? 0 : lights.get(pos.asLong());
	}
}
