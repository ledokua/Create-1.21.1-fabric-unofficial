package com.simibubi.create.compat.recipeViewer.category;

import com.simibubi.create.compat.recipeViewer.category.animations.AnimatedPress;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class PressingCategory extends CreateRecipeCategory<PressingRecipe> {

	private final AnimatedPress press = new AnimatedPress(false);

	public PressingCategory(Info<PressingRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(RecipeLayoutBuilder builder, PressingRecipe recipe) {
		builder
				.addSlot(SlotRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));

		List<ProcessingOutput> results = recipe.getRollableResults();
		int i = 0;
		for (ProcessingOutput output : results) {
			builder.addSlot(SlotRole.OUTPUT, 131 + 19 * i, 50)
					.setBackground(getRenderedSlot(output), -1, -1)
					.addItemStack(output.getStack())
					.addTooltipCallback(addStochasticTooltip(output));
			i++;
		}
	}

	@Override
	public void draw(PressingRecipe recipe, RecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 61, 41);
		AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 54);

		press.draw(graphics, getBackground().getWidth() / 2 - 17, 22);
	}

}
