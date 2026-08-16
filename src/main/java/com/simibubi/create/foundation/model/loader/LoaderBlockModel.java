package com.simibubi.create.foundation.model.loader;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.mixin.accessor.BlockModelAccessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * Base for custom-loader unbaked models: a {@link BlockModel} (so vanilla parent-chain resolution — parents *and
 * children* of these models may be plain block models — texture references, display transforms, ambient occlusion and
 * gui light all keep working) whose geometry is supplied by a subclass instead of {@code elements}.
 */
@Environment(EnvType.CLIENT)
public abstract class LoaderBlockModel extends BlockModel {

	protected final ResourceLocation id;
	protected final JsonObject json;
	@Nullable
	protected final RenderType renderType;

	protected LoaderBlockModel(ResourceLocation id, JsonObject json) {
		this(id, json, BlockModel.fromString(json.toString()));
	}

	private LoaderBlockModel(ResourceLocation id, JsonObject json, BlockModel parsed) {
		super(((BlockModelAccessor) parsed).create$getParentLocation(), ((BlockModelAccessor) parsed).create$getElements(),
			((BlockModelAccessor) parsed).create$getTextureMap(), ((BlockModelAccessor) parsed).create$getHasAmbientOcclusion(),
			((BlockModelAccessor) parsed).create$getGuiLight(), ((BlockModelAccessor) parsed).create$getTransforms(),
			((BlockModelAccessor) parsed).create$getOverrides());
		this.name = id.toString();
		this.id = id;
		this.json = json;
		this.renderType = CreateModelLoaders.renderType(GsonHelper.getAsString(json, "render_type", null));
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
		super.resolveParents(resolver);
		resolveGeometryParents(resolver);
	}

	protected void resolveGeometryParents(Function<ResourceLocation, UnbakedModel> resolver) {}

	/** Whether this model supplies geometry (as opposed to a plain elements-based helper model, e.g. composite children). */
	public abstract boolean hasCustomGeometry();

	/**
	 * Bakes this model's geometry for {@code owner}: the model actually being baked, which may be a plain child model
	 * inheriting our geometry (NeoForge: the nearest ancestor with custom geometry is baked with the child as its
	 * texture/transform/AO context — e.g. {@code item/water_wheel} → {@code block/water_wheel/item}).
	 */
	public abstract BakedModel bakeGeometry(BlockModel owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state);

	@Override
	public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
		return hasCustomGeometry() ? bakeGeometry(this, baker, spriteGetter, state) : super.bake(baker, this, spriteGetter, state, true);
	}

	/** The nearest model in {@code model}'s parent chain (itself included) that supplies custom geometry, or null. */
	@Nullable
	public static LoaderBlockModel findGeometry(BlockModel model) {
		for (BlockModel m = model; m != null; m = ((BlockModelAccessor) m).create$getParent())
			if (m instanceof LoaderBlockModel loader && loader.hasCustomGeometry())
				return loader;
		return null;
	}

	protected static TextureAtlasSprite sprite(BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, String reference) {
		return spriteGetter.apply(owner.getMaterial(reference));
	}

	protected static TextureAtlasSprite particle(BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter) {
		return sprite(owner, spriteGetter, "particle");
	}

	protected static ItemOverrides overrides(BlockModel owner, ModelBaker baker) {
		return owner.getOverrides().isEmpty() ? ItemOverrides.EMPTY : new ItemOverrides(baker, owner, owner.getOverrides());
	}
}
