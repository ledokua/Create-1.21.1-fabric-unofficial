package com.simibubi.create.foundation.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

public abstract class ChunkEvent extends LevelEvent {

	private final ChunkAccess chunk;

	protected ChunkEvent(ChunkAccess chunk, LevelAccessor level) {
		super(level);
		this.chunk = chunk;
	}

	public ChunkAccess getChunk() {
		return chunk;
	}

	public static class Load extends ChunkEvent {
		private final boolean newChunk;

		public Load(ChunkAccess chunk, LevelAccessor level, boolean newChunk) {
			super(chunk, level);
			this.newChunk = newChunk;
		}

		public boolean isNewChunk() {
			return newChunk;
		}
	}

	public static class Unload extends ChunkEvent {
		public Unload(ChunkAccess chunk, LevelAccessor level) {
			super(chunk, level);
		}
	}
}
