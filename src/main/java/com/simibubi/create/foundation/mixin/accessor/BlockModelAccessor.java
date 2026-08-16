package com.simibubi.create.foundation.mixin.accessor;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.datafixers.util.Either;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;

/** Raw (unresolved, no parent walking) BlockModel fields, for wrapping a parsed model into a custom-loader model. */
@Mixin(BlockModel.class)
public interface BlockModelAccessor {
	@Accessor("parentLocation")
	ResourceLocation create$getParentLocation();

	@Accessor("parent")
	BlockModel create$getParent();

	@Accessor("textureMap")
	Map<String, Either<Material, String>> create$getTextureMap();

	@Accessor("elements")
	List<BlockElement> create$getElements();

	@Accessor("guiLight")
	BlockModel.GuiLight create$getGuiLight();

	@Accessor("hasAmbientOcclusion")
	Boolean create$getHasAmbientOcclusion();

	@Accessor("transforms")
	ItemTransforms create$getTransforms();

	@Accessor("overrides")
	List<ItemOverride> create$getOverrides();
}
