package com.simibubi.create.compat.recipeViewer.category;

import com.simibubi.create.compat.recipeViewer.category.animations.AnimatedMillstone;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class MillingCategory extends CreateRecipeCategory<AbstractCrushingRecipe> {

	private final AnimatedMillstone millstone = new AnimatedMillstone();

	public MillingCategory(Info<AbstractCrushingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(RecipeLayoutBuilder builder, AbstractCrushingRecipe recipe) {
		builder
				.addSlot(SlotRole.INPUT, 15, 9)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));

		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		int i = 0;
		for (ProcessingOutput output : results) {
			int xOffset = i % 2 == 0 ? 0 : 19;
			int yOffset = (i / 2) * -19;

			builder
					.addSlot(SlotRole.OUTPUT, single ? 139 : 133 + xOffset, 27 + yOffset)
					.setBackground(getRenderedSlot(output), -1, -1)
					.addItemStack(output.getStack())
					.addTooltipCallback(addStochasticTooltip(output));

			i++;
		}
	}

	@Override
	public void draw(AbstractCrushingRecipe recipe, RecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_ARROW.render(graphics, 85, 32);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 43, 4);
		millstone.draw(graphics, 48, 27);
	}

}
