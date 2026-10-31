package com.simibubi.create.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.recipeViewer.SlotRole;
import com.simibubi.create.compat.recipeViewer.SlotView;
import com.simibubi.create.compat.recipeViewer.category.CreateRecipeCategory;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** One recipe of a {@link CreateRecipeCategory}, presented to EMI. */
@Environment(EnvType.CLIENT)
public class CreateEmiRecipe<T extends Recipe<?>> implements EmiRecipe {

	private final CreateRecipeCategory<T> category;
	private final EmiRecipeCategory emiCategory;
	private final RecipeHolder<T> holder;
	private final ResourceLocation id;
	private final EmiLayout layout = new EmiLayout();
	private final List<EmiIngredient> inputs = new ArrayList<>();
	private final List<EmiIngredient> catalysts = new ArrayList<>();
	private final List<EmiStack> outputs = new ArrayList<>();

	public CreateEmiRecipe(CreateRecipeCategory<T> category, EmiRecipeCategory emiCategory, RecipeHolder<T> holder) {
		this.category = category;
		this.emiCategory = emiCategory;
		this.holder = holder;
		ResourceLocation recipeId = holder.id();
		this.id = ResourceLocation.fromNamespaceAndPath(category.getId().getNamespace(),
			"/" + category.getId().getPath() + "/" + recipeId.getNamespace() + "/" + recipeId.getPath());
		category.setRecipe(layout, holder.value());
		for (EmiLayout.Slot slot : layout.slots) {
			EmiIngredient ingredient = slot.ingredient();
			if (ingredient.isEmpty())
				continue;
			switch (slot.role) {
				case INPUT -> inputs.add(ingredient);
				case CATALYST -> catalysts.add(ingredient);
				case OUTPUT -> ingredient.getEmiStacks().forEach(outputs::add);
				case RENDER_ONLY -> {}
			}
		}
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return emiCategory;
	}

	@Override
	public @Nullable ResourceLocation getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getDisplayWidth() {
		return category.getBackground().getWidth();
	}

	@Override
	public int getDisplayHeight() {
		return category.getBackground().getHeight();
	}

	@Override
	public boolean supportsRecipeTree() {
		return false; // most of Create's processing has no 1:1 crafting semantics for EMI's tree
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		// background art (slot frames) first, then the category's own drawing (arrows, animations), then the slots on top
		for (EmiLayout.Slot slot : layout.slots)
			if (slot.visible && slot.background != null) {
				var texture = slot.background;
				widgets.addDrawable(slot.x + slot.backgroundDx, slot.y + slot.backgroundDy, texture.getWidth(), texture.getHeight(),
					(graphics, mouseX, mouseY, delta) -> texture.render(graphics, 0, 0));
			}
		widgets.addDrawable(0, 0, getDisplayWidth(), getDisplayHeight(), (graphics, mouseX, mouseY, delta) ->
			category.draw(holder.value(), layout, graphics, mouseX, mouseY));
		widgets.addTooltip((mouseX, mouseY) -> category.getTooltipStrings(holder.value(), layout, mouseX, mouseY).stream()
			.map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(), 0, 0, getDisplayWidth(), getDisplayHeight());
		for (EmiLayout.Slot slot : layout.slots) {
			if (!slot.visible)
				continue;
			SlotWidget widget = new CreateSlotWidget(slot).drawBack(false);
			if (slot.role == SlotRole.OUTPUT)
				widget.recipeContext(this);
			if (slot.role == SlotRole.CATALYST)
				widget.catalyst(true);
			widgets.add(widget);
		}
	}

	/** Slot with Create's tooltip callbacks and optional custom item renderer. */
	@Environment(EnvType.CLIENT)
	static class CreateSlotWidget extends SlotWidget {
		private final EmiLayout.Slot slot;

		CreateSlotWidget(EmiLayout.Slot slot) {
			super(slot.ingredient(), slot.x - 1, slot.y - 1);
			this.slot = slot;
		}

		@Override
		public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
			List<ClientTooltipComponent> tooltip = new ArrayList<>(super.getTooltip(mouseX, mouseY));
			if (slot.tooltipCallbacks.isEmpty())
				return tooltip;
			// resolve the alternative EMI currently shows
			EmiStack shown = getStack().getEmiStacks().isEmpty() ? EmiStack.EMPTY : getStack().getEmiStacks().get(currentIndex());
			SlotView view = new SlotView() {
				@Override
				public SlotRole getRole() {
					return slot.role;
				}

				@Override
				public Optional<ItemStack> getDisplayedItem() {
					return slot.fluid || shown.isEmpty() ? Optional.empty() : Optional.of(shown.getItemStack());
				}

				@Override
				public Optional<FluidStack> getDisplayedFluid() {
					return !slot.fluid || shown.isEmpty() ? Optional.empty() : Optional.of(EmiLayout.fromEmi(shown));
				}
			};
			// JEI hands callbacks the full tooltip with the name at index 0, and Create's callbacks rely on that
			// (tooltip.add(1, ...)), so seed a placeholder name line and drop it afterwards
			Component namePlaceholder = Component.empty();
			List<Component> lines = new ArrayList<>();
			lines.add(namePlaceholder);
			for (var callback : slot.tooltipCallbacks)
				callback.addTooltip(view, lines);
			lines.removeIf(line -> line == namePlaceholder);
			// everything Create adds goes right after EMI's name line
			int insertAt = Math.min(1, tooltip.size());
			List<ClientTooltipComponent> converted = lines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
			tooltip.addAll(insertAt, converted);
			return tooltip;
		}

		private int currentIndex() {
			List<EmiStack> stacks = getStack().getEmiStacks();
			if (stacks.size() <= 1)
				return 0;
			// EMI cycles alternatives once per second, same as its own SlotWidget rendering
			return (int) ((System.currentTimeMillis() / 1000) % stacks.size());
		}

		@Override
		public void drawStack(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			if (slot.customRenderer == null) {
				super.drawStack(graphics, mouseX, mouseY, delta);
				return;
			}
			var bounds = getBounds();
			graphics.pose().pushPose();
			graphics.pose().translate(bounds.x() + 1, bounds.y() + 1, 0);
			EmiStack shown = getStack().getEmiStacks().isEmpty() ? EmiStack.EMPTY : getStack().getEmiStacks().get(currentIndex());
			slot.customRenderer.render(graphics, shown.getItemStack());
			graphics.pose().popPose();
		}
	}
}
