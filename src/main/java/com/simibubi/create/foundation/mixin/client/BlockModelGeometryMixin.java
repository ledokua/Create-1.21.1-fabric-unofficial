package com.simibubi.create.foundation.mixin.client;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.model.loader.LoaderBlockModel;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;

/**
 * NeoForge's custom-geometry inheritance: a plain block model whose parent chain contains a custom-loader model
 * (obj/composite) bakes that geometry with itself as the texture/transform context, instead of its (empty) elements.
 */
@Mixin(BlockModel.class)
public class BlockModelGeometryMixin {
	@Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Z)Lnet/minecraft/client/resources/model/BakedModel;",
		at = @At("HEAD"), cancellable = true)
	private void create$bakeInheritedGeometry(ModelBaker baker, BlockModel owner, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, boolean guiLight3d,
		CallbackInfoReturnable<BakedModel> cir) {
		BlockModel self = (BlockModel) (Object) this;
		if (self instanceof LoaderBlockModel)
			return; // handles its own baking
		LoaderBlockModel geometry = LoaderBlockModel.findGeometry(self);
		if (geometry != null)
			cir.setReturnValue(geometry.bakeGeometry(self, baker, spriteGetter, state));
	}
}
