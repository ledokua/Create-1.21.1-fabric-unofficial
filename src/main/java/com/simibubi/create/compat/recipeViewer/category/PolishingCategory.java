package com.simibubi.create.compat.recipeViewer.category;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItemComponent;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PolishingCategory extends CreateRecipeCategory<SandPaperPolishingRecipe> {

	private final ItemStack renderedSandpaper;

	public PolishingCategory(Info<SandPaperPolishingRecipe> info) {
		super(info);
		renderedSandpaper = AllItems.SAND_PAPER.asStack();
	}

	@Override
	public void setRecipe(RecipeLayoutBuilder builder, SandPaperPolishingRecipe recipe) {
		builder
				.addSlot(SlotRole.INPUT, 27, 29)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));

		ProcessingOutput output = recipe.getRollableResults().get(0);
		builder
				.addSlot(SlotRole.OUTPUT, 132, 29)
				.setBackground(getRenderedSlot(output), -1, -1)
				.addItemStack(output.getStack())
				.addTooltipCallback(addStochasticTooltip(output));
	}

	@Override
	public void draw(SandPaperPolishingRecipe recipe, RecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 61, 21);
		AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 32);

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		ItemStack[] matchingStacks = ingredients.get(0)
			.getItems();
		if (matchingStacks.length == 0)
			return;

		renderedSandpaper.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(matchingStacks[0]));
		renderedSandpaper.set(AllDataComponents.SAND_PAPER_JEI, Unit.INSTANCE);
		GuiGameElement.of(renderedSandpaper)
				.<GuiGameElement.GuiRenderBuilder>at(getBackground().getWidth() / 2 - 16, 0, 0)
				.scale(2)
				.render(graphics);
	}

}
