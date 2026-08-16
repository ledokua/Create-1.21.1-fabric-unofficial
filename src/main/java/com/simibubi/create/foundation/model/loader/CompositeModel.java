package com.simibubi.create.foundation.model.loader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.model.data.ModelData;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;

/**
 * NeoForge's {@code neoforge:composite} loader: {@code children} = named inline models, each baked on its own (with
 * its own {@code render_type}) and merged. Only the subset Create's models use (no {@code parts}/visibility, no
 * {@code item_render_order}).
 */
@Environment(EnvType.CLIENT)
public final class CompositeModel {

	private CompositeModel() {}

	@Environment(EnvType.CLIENT)
	public static final class Unbaked extends LoaderBlockModel {
		private final Map<String, LoaderBlockModel> children = new LinkedHashMap<>();

		public Unbaked(ResourceLocation id, JsonObject json) {
			super(id, json);
			JsonObject childrenJson = GsonHelper.getAsJsonObject(json, "children");
			for (Map.Entry<String, com.google.gson.JsonElement> entry : childrenJson.entrySet()) {
				ResourceLocation childId = id.withSuffix("/" + entry.getKey());
				children.put(entry.getKey(), new Child(childId, entry.getValue().getAsJsonObject()));
			}
		}

		@Override
		protected void resolveGeometryParents(Function<ResourceLocation, UnbakedModel> resolver) {
			children.values().forEach(child -> child.resolveParents(resolver));
		}

		@Override
		public boolean hasCustomGeometry() {
			return true;
		}

		@Override
		public BakedModel bakeGeometry(BlockModel owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
			Map<RenderType, LoaderBakedModel.Bucket> buckets = new HashMap<>();
			RandomSource random = RandomSource.create();
			for (LoaderBlockModel child : children.values()) {
				BakedModel baked = child.bake(baker, spriteGetter, state);
				RenderType layer = child.renderType != null ? child.renderType : renderType;
				LoaderBakedModel.Bucket bucket = buckets.computeIfAbsent(layer, $ -> new LoaderBakedModel.Bucket());
				for (Direction side : Direction.values()) {
					random.setSeed(42);
					for (BakedQuad quad : baked.getQuads(null, side, random, ModelData.EMPTY, null))
						bucket.add(side, quad);
				}
				random.setSeed(42);
				for (BakedQuad quad : baked.getQuads(null, null, random, ModelData.EMPTY, null))
					bucket.add(null, quad);
			}
			if (buckets.isEmpty())
				buckets.put(renderType, new LoaderBakedModel.Bucket());
			return new LoaderBakedModel(buckets, owner.hasAmbientOcclusion(), true, owner.getGuiLight().lightLikeBlock(), particle(owner, spriteGetter), owner.getTransforms(), overrides(owner, baker));
		}
	}

	/** A child: a plain (elements-based) block model that just remembers its render_type; baked by vanilla. */
	@Environment(EnvType.CLIENT)
	static final class Child extends LoaderBlockModel {
		Child(ResourceLocation id, JsonObject json) {
			super(id, json);
		}

		@Override
		public boolean hasCustomGeometry() {
			return false;
		}

		@Override
		public BakedModel bakeGeometry(BlockModel owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
			throw new UnsupportedOperationException();
		}
	}
}
