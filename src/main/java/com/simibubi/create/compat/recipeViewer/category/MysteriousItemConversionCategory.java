package com.simibubi.create.compat.recipeViewer.category;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.recipeViewer.ConversionRecipe;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class MysteriousItemConversionCategory extends CreateRecipeCategory<ConversionRecipe> {

	public static final List<RecipeHolder<ConversionRecipe>> RECIPES = new ArrayList<>();

	static {
		RECIPES.add(ConversionRecipe.create(AllItems.EMPTY_BLAZE_BURNER.asStack(), AllBlocks.BLAZE_BURNER.asStack()));
		RECIPES.add(ConversionRecipe.create(AllBlocks.PECULIAR_BELL.asStack(), AllBlocks.HAUNTED_BELL.asStack()));
//		RECIPES.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.SHADOW_STEEL.asStack()));
//		RECIPES.add(ConversionRecipe.create(AllItems.CHROMATIC_COMPOUND.asStack(), AllItems.REFINED_RADIANCE.asStack()));
	}

	public MysteriousItemConversionCategory(Info<ConversionRecipe> info) {
		super(info);
	}

	@Override
	public void setRecipe(RecipeLayoutBuilder builder, ConversionRecipe recipe) {
		builder
				.addSlot(SlotRole.INPUT, 27, 17)
				.setBackground(getRenderedSlot(), -1, -1)
				.addIngredients(recipe.getIngredients().get(0));
		builder
				.addSlot(SlotRole.OUTPUT, 132, 17)
				.setBackground(getRenderedSlot(), -1, -1)
				.addItemStack(recipe.getRollableResults().get(0).getStack());
	}

	@Override
	public void draw(ConversionRecipe recipe, RecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 20);
		AllGuiTextures.JEI_QUESTION_MARK.render(graphics, 77, 5);
	}

}
