package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.compat.recipeViewer.Icon;
import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlot;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotItemRenderer;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import com.simibubi.create.compat.recipeViewer.SlotTooltipCallback;
import com.simibubi.create.compat.recipeViewer.SlotView;
import com.simibubi.create.compat.recipeViewer.category.CreateRecipeCategory;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Presents a viewer-neutral {@link CreateRecipeCategory} to JEI. */
public class JeiCategoryAdapter<T extends Recipe<?>> implements IRecipeCategory<RecipeHolder<T>> {

	public final CreateRecipeCategory<T> category;
	public final RecipeType<RecipeHolder<T>> type;
	private final IDrawable background;
	private final IDrawable icon;

	public JeiCategoryAdapter(CreateRecipeCategory<T> category) {
		this.category = category;
		this.type = RecipeType.createRecipeHolderType(category.getId());
		this.background = drawable(category.getBackground());
		this.icon = drawable(category.getIcon());
	}

	@Override
	@NotNull
	public RecipeType<RecipeHolder<T>> getRecipeType() {
		return type;
	}

	@Override
	public Component getTitle() {
		return category.getTitle();
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<T> holder, IFocusGroup focuses) {
		category.setRecipe(new LayoutBuilder(builder), holder.value());
	}

	@Override
	public void draw(RecipeHolder<T> holder, IRecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY) {
		category.draw(holder.value(), new SlotsView(recipeSlotsView), gui, mouseX, mouseY);
	}

	@Override
	public List<Component> getTooltipStrings(RecipeHolder<T> holder, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return category.getTooltipStrings(holder.value(), new SlotsView(recipeSlotsView), mouseX, mouseY);
	}

	public static IDrawable drawable(Icon icon) {
		return new IDrawable() {
			@Override
			public int getWidth() {
				return icon.getWidth();
			}

			@Override
			public int getHeight() {
				return icon.getHeight();
			}

			@Override
			public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
				icon.draw(graphics, xOffset, yOffset);
			}
		};
	}

	static RecipeIngredientRole role(SlotRole role) {
		return switch (role) {
			case INPUT -> RecipeIngredientRole.INPUT;
			case OUTPUT -> RecipeIngredientRole.OUTPUT;
			case CATALYST -> RecipeIngredientRole.CATALYST;
			case RENDER_ONLY -> RecipeIngredientRole.RENDER_ONLY;
		};
	}

	static SlotRole role(RecipeIngredientRole role) {
		return switch (role) {
			case INPUT -> SlotRole.INPUT;
			case OUTPUT -> SlotRole.OUTPUT;
			case CATALYST -> SlotRole.CATALYST;
			case RENDER_ONLY -> SlotRole.RENDER_ONLY;
		};
	}

	record LayoutBuilder(IRecipeLayoutBuilder builder) implements RecipeLayoutBuilder {
		@Override
		public RecipeSlot addSlot(SlotRole role, int x, int y) {
			return new Slot(builder.addSlot(role(role), x, y));
		}

		@Override
		public RecipeSlot addInvisibleIngredients(SlotRole role) {
			return new InvisibleSlot(builder.addInvisibleIngredients(role(role)));
		}
	}

	/** JEI's invisible ingredients only accept ingredients; the rest is ignored. */
	record InvisibleSlot(mezz.jei.api.gui.builder.IIngredientAcceptor<?> acceptor) implements RecipeSlot {
		@Override
		public RecipeSlot addItemStack(ItemStack stack) {
			acceptor.addItemStack(stack);
			return this;
		}

		@Override
		public RecipeSlot addItemStacks(List<ItemStack> stacks) {
			acceptor.addItemStacks(stacks);
			return this;
		}

		@Override
		public RecipeSlot addFluids(List<FluidStack> fluids, int displayAmount) {
			acceptor.addIngredients(CreateJeiIngredientTypes.FLUID_STACK, CreateJeiIngredientTypes.toJei(fluids));
			return this;
		}

		@Override
		public RecipeSlot setBackground(AllGuiTextures texture, int dx, int dy) {
			return this;
		}

		@Override
		public RecipeSlot addTooltipCallback(SlotTooltipCallback callback) {
			return this;
		}

		@Override
		public RecipeSlot setCustomRenderer(SlotItemRenderer renderer) {
			return this;
		}
	}

	record Slot(IRecipeSlotBuilder slot) implements RecipeSlot {
		@Override
		public RecipeSlot addItemStack(ItemStack stack) {
			slot.addItemStack(stack);
			return this;
		}

		@Override
		public RecipeSlot addItemStacks(List<ItemStack> stacks) {
			slot.addItemStacks(stacks);
			return this;
		}

		@Override
		public RecipeSlot addFluids(List<FluidStack> fluids, int displayAmount) {
			slot.addIngredients(CreateJeiIngredientTypes.FLUID_STACK, CreateJeiIngredientTypes.toJei(fluids));
			slot.setFluidRenderer(CreateJeiIngredientTypes.toDroplets(displayAmount), false, 16, 16); // fluid fills the whole slot
			return this;
		}

		@Override
		public RecipeSlot setBackground(AllGuiTextures texture, int dx, int dy) {
			slot.setBackground(drawable(CreateRecipeCategory.asIcon(texture)), dx, dy);
			return this;
		}

		@Override
		public RecipeSlot addTooltipCallback(SlotTooltipCallback callback) {
			slot.addRichTooltipCallback((view, tooltip) -> {
				List<Component> lines = new ArrayList<>();
				callback.addTooltip(new SlotViewImpl(view), lines);
				lines.forEach(tooltip::add);
			});
			return this;
		}

		@Override
		public RecipeSlot setCustomRenderer(SlotItemRenderer renderer) {
			slot.setCustomRenderer(VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
				@Override
				public void render(GuiGraphics graphics, ItemStack ingredient) {
					renderer.render(graphics, ingredient);
				}

				@Override
				public List<Component> getTooltip(ItemStack ingredient, net.minecraft.world.item.TooltipFlag tooltipFlag) {
					return ingredient.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(net.minecraft.client.Minecraft.getInstance().level),
						net.minecraft.client.Minecraft.getInstance().player, tooltipFlag);
				}

				@Override
				public int getWidth() {
					return renderer.getWidth();
				}

				@Override
				public int getHeight() {
					return renderer.getHeight();
				}
			});
			return this;
		}
	}

	record SlotViewImpl(IRecipeSlotView view) implements SlotView {
		@Override
		public SlotRole getRole() {
			return role(view.getRole());
		}

		@Override
		public Optional<ItemStack> getDisplayedItem() {
			return view.getDisplayedItemStack();
		}

		@Override
		public Optional<FluidStack> getDisplayedFluid() {
			return view.getDisplayedIngredient(CreateJeiIngredientTypes.FLUID_STACK).map(CreateJeiIngredientTypes::fromJei);
		}
	}

	record SlotsView(IRecipeSlotsView view) implements RecipeSlotsView {
		@Override
		public List<SlotView> getSlotViews() {
			return view.getSlotViews().stream().<SlotView>map(SlotViewImpl::new).toList();
		}
	}
}
