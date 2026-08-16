package com.simibubi.create.compat.emi;

import com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.equipment.blueprint.BlueprintMenu;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/** "Fill" button on crafting recipes while a crafting blueprint is open: assigns the recipe to the blueprint. */
@Environment(EnvType.CLIENT)
public class BlueprintEmiRecipeHandler implements EmiRecipeHandler<BlueprintMenu> {

	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<BlueprintMenu> screen) {
		return new EmiPlayerInventory(java.util.List.of());
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.getBackingRecipe() != null;
	}

	@Override
	public boolean alwaysDisplaySupport(EmiRecipe recipe) {
		return true;
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<BlueprintMenu> context) {
		return recipe.getBackingRecipe() != null;
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<BlueprintMenu> context) {
		if (recipe.getBackingRecipe() == null)
			return false;
		CatnipServices.NETWORK.sendToServer(new BlueprintAssignCompleteRecipePacket(recipe.getBackingRecipe().id()));
		return true;
	}
}
