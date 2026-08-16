package com.simibubi.create.content.processing.sequenced;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.simibubi.create.compat.recipeViewer.category.sequencedAssembly.SequencedAssemblySubCategory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.simibubi.create.foundation.transfer.fluid.crafting.SizedFluidIngredient;

public interface IAssemblyRecipe {
	default boolean supportsAssembly() {
		return true;
	}

	@Environment(EnvType.CLIENT)
	Component getDescriptionForAssembly();

	void addRequiredMachines(Set<ItemLike> list);

	void addAssemblyIngredients(List<Ingredient> list);

	default void addAssemblyFluidIngredients(List<SizedFluidIngredient> list) {
	}

	Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory();
}
