package com.simibubi.create.compat.emi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.compat.recipeViewer.CreateRecipeCategories;
import com.simibubi.create.compat.recipeViewer.Icon;
import com.simibubi.create.compat.recipeViewer.IngredientAccess;
import com.simibubi.create.compat.recipeViewer.ToolboxColoringRecipeMaker;
import com.simibubi.create.compat.recipeViewer.category.CreateRecipeCategory;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.filter.AttributeFilterScreen;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.createmod.catnip.platform.CatnipServices;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;

/**
 * Native EMI support ({@code emi} entrypoint), built on the same viewer-neutral categories as the JEI plugin.
 * Covers all recipe categories, workstations, exclusion areas of Create's screens and ghost-item drag &amp; drop into
 * filter/blueprint/controller/schedule screens.
 */
@Environment(EnvType.CLIENT)
public class CreateEmiPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		IngredientAccess ingredients = new IngredientAccess() {
			@Override
			public Collection<ItemStack> allItems() {
				List<ItemStack> list = new ArrayList<>();
				for (EmiStack stack : dev.emi.emi.api.EmiApi.getIndexStacks())
					if (stack.getKey() instanceof net.minecraft.world.item.Item)
						list.add(stack.getItemStack());
				return list;
			}

			@Override
			public Collection<FluidStack> allFluids() {
				List<FluidStack> list = new ArrayList<>();
				for (EmiStack stack : dev.emi.emi.api.EmiApi.getIndexStacks())
					if (stack.getKey() instanceof Fluid)
						list.add(EmiLayout.fromEmi(stack));
				return list;
			}
		};

		List<CreateRecipeCategory<?>> categories = CreateRecipeCategories.create(ingredients);
		int recipes = 0;
		for (CreateRecipeCategory<?> category : categories)
			recipes += registerCategory(registry, category);
		Create.LOGGER.info("EMI: registered {} categories with {} recipes", categories.size(), recipes);

		// toolbox dyeing shows up as vanilla crafting
		ToolboxColoringRecipeMaker.createRecipes().forEach(holder ->
			registry.addRecipe(new dev.emi.emi.api.recipe.EmiCraftingRecipe(
				holder.value().getIngredients().stream().map(EmiIngredient::of).toList(),
				EmiStack.of(holder.value().getResultItem(net.minecraft.client.Minecraft.getInstance().level.registryAccess())),
				holder.id(), true)));

		// keep EMI's panels off Create's side widgets
		registry.addGenericExclusionArea((screen, consumer) -> {
			if (screen instanceof AbstractSimiContainerScreen<?> simi)
				for (Rect2i area : simi.getExtraAreas())
					consumer.accept(new Bounds(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
		});

		// ghost items: drag from EMI onto filter/blueprint/etc. slots
		EmiDragDropHandler<net.minecraft.client.gui.screens.Screen> ghostHandler = (screen, ingredient, x, y) -> {
			if (!(screen instanceof AbstractSimiContainerScreen<?> gui) || !(gui.getMenu() instanceof GhostItemMenu<?> menu))
				return false;
			if (ingredient.getEmiStacks().isEmpty() || !(ingredient.getEmiStacks().get(0).getKey() instanceof net.minecraft.world.item.Item))
				return false;
			ItemStack stack = ingredient.getEmiStacks().get(0).getItemStack().copy();
			stack.setCount(1);
			boolean isAttributeFilter = gui instanceof AttributeFilterScreen;
			for (int i = 36; i < menu.slots.size(); i++) {
				Slot slot = menu.slots.get(i);
				if (!slot.isActive())
					continue;
				int left = gui.getGuiLeft() + slot.x, top = gui.getGuiTop() + slot.y;
				if (x >= left && x < left + 16 && y >= top && y < top + 16) {
					int slotIndex = i - 36;
					menu.ghostInventory.setStackInSlot(slotIndex, stack);
					if (!isAttributeFilter)
						CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, slotIndex));
					return true;
				}
				if (isAttributeFilter)
					break; // only the first slot takes items
			}
			return false;
		};
		registry.addDragDropHandler(AbstractFilterScreen.class, ghostHandler::dropStack);
		registry.addDragDropHandler(BlueprintScreen.class, ghostHandler::dropStack);
		registry.addDragDropHandler(LinkedControllerScreen.class, ghostHandler::dropStack);
		registry.addDragDropHandler(ScheduleScreen.class, ghostHandler::dropStack);
		registry.addDragDropHandler(RedstoneRequesterScreen.class, ghostHandler::dropStack);
		registry.addDragDropHandler(FactoryPanelSetItemScreen.class, ghostHandler::dropStack);
	}

	private static <T extends Recipe<?>> int registerCategory(EmiRegistry registry, CreateRecipeCategory<T> category) {
		EmiRecipeCategory emiCategory = new EmiRecipeCategory(category.getId(), renderable(category.getIcon()), renderable(category.getIcon())) {
			@Override
			public net.minecraft.network.chat.Component getName() {
				return category.getTitle();
			}
		};
		registry.addCategory(emiCategory);
		for (Supplier<? extends ItemStack> catalyst : category.getCatalysts())
			registry.addWorkstation(emiCategory, EmiStack.of(catalyst.get()));
		int count = 0;
		List<RecipeHolder<T>> holders;
		try {
			holders = category.getRecipes();
		} catch (RuntimeException e) {
			Create.LOGGER.error("Failed to collect recipes for EMI category {}", category.getId(), e);
			return 0;
		}
		for (RecipeHolder<T> holder : holders) {
			try {
				registry.addRecipe(new CreateEmiRecipe<>(category, emiCategory, holder));
				count++;
			} catch (RuntimeException e) {
				Create.LOGGER.error("Failed to add recipe {} to EMI category {}", holder.id(), category.getId(), e);
			}
		}
		return count;
	}

	private static EmiRenderable renderable(Icon icon) {
		return (graphics, x, y, delta) -> icon.draw(graphics, x, y);
	}
}
