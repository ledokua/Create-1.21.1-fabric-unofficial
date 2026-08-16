package com.simibubi.create.compat.recipeViewer.category;

import com.simibubi.create.compat.recipeViewer.category.animations.AnimatedDeployer;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlot;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class DeployingCategory extends CreateRecipeCategory<DeployerApplicationRecipe> {

	private final AnimatedDeployer deployer = new AnimatedDeployer();

	public DeployingCategory(Info<DeployerApplicationRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(RecipeLayoutBuilder builder, DeployerApplicationRecipe recipe) {
		builder
				.addSlot(SlotRole.INPUT, 27, 51)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getProcessedItem());
		RecipeSlot handItemSlot = builder
				.addSlot(SlotRole.INPUT, 51, 5)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getRequiredHeldItem());

		List<ProcessingOutput> results = recipe.getRollableResults();
		boolean single = results.size() == 1;
		for (int i = 0; i < results.size(); i++) {
			ProcessingOutput output = results.get(i);
			int xOffset = i % 2 == 0 ? 0 : 19;
			int yOffset = (i / 2) * -19;
			builder.addSlot(SlotRole.OUTPUT, single ? 132 : 132 + xOffset, 51 + yOffset)
				.setBackground(getRenderedSlot(output), -1, -1)
				.addItemStack(output.getStack())
				.addTooltipCallback(addStochasticTooltip(output));
		}

		if (recipe.shouldKeepHeldItem())
			handItemSlot.addTooltipCallback((recipeSlotView, tooltip) -> tooltip.add(1, CreateLang.translateDirect("recipe.deploying.not_consumed").withStyle(ChatFormatting.GOLD)));

	}

	@Override
	public void draw(DeployerApplicationRecipe recipe, RecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
		AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 126, 29 + (recipe.getRollableResults().size() > 2 ? -19 : 0));
		deployer.draw(graphics, getBackground().getWidth() / 2 - 13, 22);
	}

}
