package com.tterrag.registrate.providers;

import com.simibubi.create.foundation.data.modelgen.ExistingFileHelper;
import com.simibubi.create.foundation.data.modelgen.ItemModelBuilder;
import com.simibubi.create.foundation.data.modelgen.ItemModelProvider;
import com.simibubi.create.foundation.data.modelgen.ModelFile;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/** Shell — see {@link RegistrateBlockstateProvider}. Adds Registrate's item-model helpers on top of the model DSL. */
public class RegistrateItemModelProvider extends ItemModelProvider {

	public RegistrateItemModelProvider(String modid, ExistingFileHelper existingFileHelper) {
		super(modid, existingFileHelper);
	}

	public String modid(NonNullSupplier<? extends ItemLike> item) {
		return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getNamespace();
	}

	public String name(NonNullSupplier<? extends ItemLike> item) {
		return BuiltInRegistries.ITEM.getKey(item.get().asItem()).getPath();
	}

	public ResourceLocation itemTexture(NonNullSupplier<? extends ItemLike> item) {
		return modLoc("item/" + name(item));
	}

	public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block) {
		return blockItem(block, "");
	}

	public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block, String suffix) {
		return withExistingParent(name(block), ResourceLocation.fromNamespaceAndPath(modid(block), "block/" + name(block) + suffix));
	}

	public ItemModelBuilder blockWithInventoryModel(NonNullSupplier<? extends ItemLike> block) {
		return withExistingParent(name(block), ResourceLocation.fromNamespaceAndPath(modid(block), "block/" + name(block) + "_inventory"));
	}

	public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block) {
		return blockSprite(block, modLoc("block/" + name(block)));
	}

	public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block, ResourceLocation texture) {
		return generated(() -> block.get().asItem(), texture);
	}

	public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item) {
		return generated(item, itemTexture(item));
	}

	public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item, ResourceLocation... layers) {
		return getBuilder(name(item));
	}

	public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item) {
		return handheld(item, itemTexture(item));
	}

	public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item, ResourceLocation texture) {
		return getBuilder(name(item));
	}

	public String getName() {
		return "Item models";
	}
}
