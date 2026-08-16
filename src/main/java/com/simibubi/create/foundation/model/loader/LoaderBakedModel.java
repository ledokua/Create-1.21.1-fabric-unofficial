package com.simibubi.create.foundation.model.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.model.ChunkRenderTypeSet;
import com.simibubi.create.foundation.model.IDynamicBakedModel;
import com.simibubi.create.foundation.model.data.ModelData;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Baked model produced by the custom loaders: quads bucketed by cull face and by render type (a {@code null} render
 * type bucket means "whatever the block's layer is"). Implements the NeoForge-shaped {@link IDynamicBakedModel} so
 * both the FRAPI path (per-layer emission) and vanilla/item paths work.
 */
@Environment(EnvType.CLIENT)
public class LoaderBakedModel implements IDynamicBakedModel {

	/** Quads of one render-type bucket. */
	static final class Bucket {
		final List<BakedQuad> unculled = new ArrayList<>();
		final Map<Direction, List<BakedQuad>> culled = new EnumMap<>(Direction.class);

		void add(@Nullable Direction cull, BakedQuad quad) {
			if (cull == null)
				unculled.add(quad);
			else
				culled.computeIfAbsent(cull, $ -> new ArrayList<>()).add(quad);
		}

		List<BakedQuad> get(@Nullable Direction side) {
			return side == null ? unculled : culled.getOrDefault(side, Collections.emptyList());
		}

		boolean isEmpty() {
			return unculled.isEmpty() && culled.isEmpty();
		}
	}

	private final Map<RenderType, Bucket> buckets; // key null = default layer
	private final Bucket defaultBucket;
	private final boolean ambientOcclusion;
	private final boolean gui3d;
	private final boolean usesBlockLight;
	private final TextureAtlasSprite particle;
	private final ItemTransforms transforms;
	private final ItemOverrides overrides;
	private final ChunkRenderTypeSet explicitRenderTypes;

	LoaderBakedModel(Map<RenderType, Bucket> buckets, boolean ambientOcclusion, boolean gui3d, boolean usesBlockLight,
		TextureAtlasSprite particle, ItemTransforms transforms, ItemOverrides overrides) {
		this.buckets = buckets;
		this.defaultBucket = buckets.getOrDefault(null, new Bucket());
		this.ambientOcclusion = ambientOcclusion;
		this.gui3d = gui3d;
		this.usesBlockLight = usesBlockLight;
		this.particle = particle;
		this.transforms = transforms;
		this.overrides = overrides;
		List<RenderType> explicit = new ArrayList<>();
		for (Map.Entry<RenderType, Bucket> e : buckets.entrySet())
			if (e.getKey() != null && !e.getValue().isEmpty())
				explicit.add(e.getKey());
		this.explicitRenderTypes = explicit.isEmpty() ? null : ChunkRenderTypeSet.of(explicit);
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
		if (renderType == null) {
			// layer-less query (items, particles, our own helpers): everything
			if (buckets.size() == 1)
				return defaultBucket.get(side);
			List<BakedQuad> all = new ArrayList<>();
			for (Bucket bucket : buckets.values())
				all.addAll(bucket.get(side));
			return all;
		}
		Bucket explicit = buckets.get(renderType);
		if (explicit != null) {
			if (explicitRenderTypes != null && !defaultBucket.isEmpty() && matchesDefaultLayer(state, renderType)) {
				List<BakedQuad> both = new ArrayList<>(explicit.get(side));
				both.addAll(defaultBucket.get(side));
				return both;
			}
			return explicit.get(side);
		}
		return matchesDefaultLayer(state, renderType) ? defaultBucket.get(side) : Collections.emptyList();
	}

	private boolean matchesDefaultLayer(@Nullable BlockState state, RenderType renderType) {
		if (state == null)
			return true;
		return IDynamicBakedModel.super.getRenderTypes(state, RandomSource.create(), ModelData.EMPTY).contains(renderType);
	}

	@Override
	public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
		ChunkRenderTypeSet base = defaultBucket.isEmpty() && explicitRenderTypes != null ? ChunkRenderTypeSet.none() : IDynamicBakedModel.super.getRenderTypes(state, rand, data);
		return explicitRenderTypes == null ? base : ChunkRenderTypeSet.union(base, explicitRenderTypes);
	}

	@Override
	public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
		return IDynamicBakedModel.super.getRenderTypes(itemStack, fabulous);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return ambientOcclusion;
	}

	@Override
	public boolean isGui3d() {
		return gui3d;
	}

	@Override
	public boolean usesBlockLight() {
		return usesBlockLight;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon() {
		return particle;
	}

	@Override
	public @NotNull ItemTransforms getTransforms() {
		return transforms;
	}

	@Override
	public @NotNull ItemOverrides getOverrides() {
		return overrides;
	}
}
