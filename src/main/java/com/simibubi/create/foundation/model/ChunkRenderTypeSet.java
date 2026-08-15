package com.simibubi.create.foundation.model;

import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.minecraft.client.renderer.RenderType;

/** Immutable set of chunk (block) render layers. Create-owned (was NeoForge's). */
public final class ChunkRenderTypeSet implements Iterable<RenderType> {

	private static final List<RenderType> CHUNK_RENDER_TYPES_LIST = RenderType.chunkBufferLayers();
	private static final RenderType[] CHUNK_RENDER_TYPES = CHUNK_RENDER_TYPES_LIST.toArray(new RenderType[0]);
	private static final ChunkRenderTypeSet NONE = new ChunkRenderTypeSet(new BitSet());
	private static final ChunkRenderTypeSet ALL = new ChunkRenderTypeSet(allBits());

	private static BitSet allBits() {
		BitSet bits = new BitSet();
		bits.set(0, CHUNK_RENDER_TYPES.length);
		return bits;
	}

	public static ChunkRenderTypeSet none() {
		return NONE;
	}

	public static ChunkRenderTypeSet all() {
		return ALL;
	}

	public static ChunkRenderTypeSet of(RenderType... renderTypes) {
		return of(List.of(renderTypes));
	}

	public static ChunkRenderTypeSet of(Collection<RenderType> renderTypes) {
		if (renderTypes.isEmpty())
			return none();
		BitSet bits = new BitSet();
		for (RenderType renderType : renderTypes) {
			int index = CHUNK_RENDER_TYPES_LIST.indexOf(renderType);
			if (index < 0)
				throw new IllegalArgumentException("Attempted to create chunk render type set with a non-chunk render type: " + renderType);
			bits.set(index);
		}
		return new ChunkRenderTypeSet(bits);
	}

	public static ChunkRenderTypeSet union(ChunkRenderTypeSet... sets) {
		return union(List.of(sets));
	}

	public static ChunkRenderTypeSet union(Collection<ChunkRenderTypeSet> sets) {
		if (sets.isEmpty())
			return none();
		BitSet bits = new BitSet();
		for (ChunkRenderTypeSet set : sets)
			bits.or(set.bits);
		return new ChunkRenderTypeSet(bits);
	}

	public static ChunkRenderTypeSet intersection(ChunkRenderTypeSet... sets) {
		return intersection(List.of(sets));
	}

	public static ChunkRenderTypeSet intersection(Collection<ChunkRenderTypeSet> sets) {
		if (sets.isEmpty())
			return all();
		BitSet bits = new BitSet();
		bits.set(0, CHUNK_RENDER_TYPES.length);
		for (ChunkRenderTypeSet set : sets)
			bits.and(set.bits);
		return new ChunkRenderTypeSet(bits);
	}

	private final BitSet bits;

	private ChunkRenderTypeSet(BitSet bits) {
		this.bits = bits;
	}

	public boolean isEmpty() {
		return bits.isEmpty();
	}

	public boolean contains(RenderType renderType) {
		int index = CHUNK_RENDER_TYPES_LIST.indexOf(renderType);
		return index >= 0 && bits.get(index);
	}

	public List<RenderType> asList() {
		java.util.ArrayList<RenderType> list = new java.util.ArrayList<>();
		forEach(list::add);
		return List.copyOf(list);
	}

	@Override
	public Iterator<RenderType> iterator() {
		return new Iterator<>() {
			private int index = bits.nextSetBit(0);

			@Override
			public boolean hasNext() {
				return index >= 0;
			}

			@Override
			public RenderType next() {
				if (index < 0)
					throw new NoSuchElementException();
				RenderType type = CHUNK_RENDER_TYPES[index];
				index = bits.nextSetBit(index + 1);
				return type;
			}
		};
	}
}
