package com.simibubi.create.compat.emi;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.foundation.utility.CreateLang;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Stock keeper: the recipe "fill" button orders a craft from the stock network (port of JEI's
 * StockKeeperTransferHandler). EMI's inventory view is the network's stock, so its own availability check applies.
 */
@Environment(EnvType.CLIENT)
public class StockKeeperEmiRecipeHandler implements EmiRecipeHandler<StockKeeperRequestMenu> {

	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<StockKeeperRequestMenu> screen) {
		List<EmiStack> stacks = new ArrayList<>();
		InventorySummary summary = screen.getMenu().contentHolder.getLastClientsideStockSnapshotAsSummary();
		if (summary != null)
			for (BigItemStack big : summary.getStacksByCount())
				stacks.add(EmiStack.of(big.stack, big.count));
		return new EmiPlayerInventory(stacks);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		RecipeHolder<?> backing = recipe.getBackingRecipe();
		return backing != null && backing.value().getIngredients().size() <= 9;
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<StockKeeperRequestMenu> context) {
		return supportsRecipe(recipe) && context.getInventory().canCraft(recipe);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<StockKeeperRequestMenu> context) {
		if (!(context.getScreen() instanceof StockKeeperRequestScreen screen) || recipe.getBackingRecipe() == null)
			return List.of();
		Recipe<?> backing = recipe.getBackingRecipe().value();
		for (CraftableBigItemStack cbis : screen.recipesToOrder)
			if (cbis.recipe == backing)
				return List.of(ClientTooltipComponent.create(CreateLang.translate("gui.stock_keeper.already_ordering_recipe").component().getVisualOrderText()));
		if (screen.itemsToOrder.size() >= 9)
			return List.of(ClientTooltipComponent.create(CreateLang.translate("gui.stock_keeper.slots_full").component().getVisualOrderText()));
		if (!context.getInventory().canCraft(recipe))
			return List.of(ClientTooltipComponent.create(CreateLang.translate("gui.stock_keeper.not_in_stock").component().getVisualOrderText()));
		return List.of();
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<StockKeeperRequestMenu> context) {
		if (!(context.getScreen() instanceof StockKeeperRequestScreen screen) || recipe.getBackingRecipe() == null)
			return false;
		Recipe<?> backing = recipe.getBackingRecipe().value();
		for (CraftableBigItemStack cbis : screen.recipesToOrder)
			if (cbis.recipe == backing)
				return false;
		if (screen.itemsToOrder.size() >= 9)
			return false;
		ItemStack result = backing.getResultItem(screen.getMenu().player.level().registryAccess());
		if (result.isEmpty())
			return false;
		CraftableBigItemStack cbis = new CraftableBigItemStack(result, backing);
		screen.recipesToOrder.add(cbis);
		screen.searchBox.setValue("");
		screen.refreshSearchNextTick = true;
		screen.requestCraftable(cbis, Math.max(1, context.getAmount()));
		return true;
	}
}
