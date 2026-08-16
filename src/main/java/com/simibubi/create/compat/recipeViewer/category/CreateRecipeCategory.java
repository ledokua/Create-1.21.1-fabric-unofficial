package com.simibubi.create.compat.recipeViewer.category;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.recipeViewer.RecipeCollector;
import com.simibubi.create.compat.recipeViewer.DoubleItemIcon;
import com.simibubi.create.compat.recipeViewer.EmptyBackground;
import com.simibubi.create.compat.recipeViewer.ItemIcon;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.foundation.utility.CreateLang;

import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlot;
import com.simibubi.create.compat.recipeViewer.Icon;
import com.simibubi.create.compat.recipeViewer.SlotTooltipCallback;
import com.simibubi.create.compat.recipeViewer.SlotView;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import net.createmod.catnip.config.ConfigBase.ConfigBool;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.ItemLike;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;
import com.simibubi.create.foundation.transfer.fluid.crafting.SizedFluidIngredient;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CreateRecipeCategory<T extends Recipe<?>> {
	private static final AllGuiTextures BASIC_SLOT = AllGuiTextures.JEI_SLOT;
	private static final AllGuiTextures CHANCE_SLOT = AllGuiTextures.JEI_CHANCE_SLOT;

	protected final ResourceLocation id;
	protected final Class<? extends T> recipeClass;
	protected final Component title;
	protected final Icon background;
	protected final Icon icon;

	private final Supplier<List<RecipeHolder<T>>> recipes;
	private final List<Supplier<? extends ItemStack>> catalysts;

	public CreateRecipeCategory(Info<T> info) {
		this.id = info.id();
		this.recipeClass = info.recipeClass();
		this.title = info.title();
		this.background = info.background();
		this.icon = info.icon();
		this.recipes = info.recipes();
		this.catalysts = info.catalysts();
	}

	public ResourceLocation getId() {
		return id;
	}

	public Class<? extends T> getRecipeClass() {
		return recipeClass;
	}

	public Component getTitle() {
		return title;
	}

	public Icon getBackground() {
		return background;
	}

	public Icon getIcon() {
		return icon;
	}

	/** Recipes shown in this category (collected lazily; call once the client has its recipe manager). */
	public List<RecipeHolder<T>> getRecipes() {
		return recipes.get();
	}

	public List<Supplier<? extends ItemStack>> getCatalysts() {
		return catalysts;
	}

	public abstract void setRecipe(RecipeLayoutBuilder builder, T recipe);

	public abstract void draw(T recipe, RecipeSlotsView recipeSlotsView, GuiGraphics gui, double mouseX, double mouseY);

	public List<Component> getTooltipStrings(T recipe, RecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return List.of();
	}

	public static AllGuiTextures getRenderedSlot() {
		return BASIC_SLOT;
	}

	public static AllGuiTextures getRenderedSlot(ProcessingOutput output) {
		return getRenderedSlot(output.getChance());
	}

	public static AllGuiTextures getRenderedSlot(float chance) {
		if (chance == 1)
			return BASIC_SLOT;

		return CHANCE_SLOT;
	}

	public static ItemStack getResultItem(Recipe<?> recipe) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return ItemStack.EMPTY;
		return recipe.getResultItem(level.registryAccess());
	}

	public static SlotTooltipCallback addStochasticTooltip(ProcessingOutput output) {
		return (view, tooltip) -> {
			float chance = output.getChance();
			if (chance != 1)
				tooltip.add(CreateLang.translateDirect("recipe.processing.chance", chance < 0.01 ? "<1" : (int) (chance * 100))
					.withStyle(ChatFormatting.GOLD));
		};
	}

	public static RecipeSlot addFluidSlot(RecipeLayoutBuilder builder, int x, int y, SizedFluidIngredient ingredient) {
		return builder.addSlot(SlotRole.INPUT, x, y)
			.setBackground(getRenderedSlot(), -1, -1)
			.addFluids(Arrays.asList(ingredient.getFluids()), ingredient.amount())
			.addTooltipCallback(CreateRecipeCategory::addPotionTooltip);
	}

	public static RecipeSlot addFluidSlot(RecipeLayoutBuilder builder, int x, int y, FluidStack stack) {
		return builder.addSlot(SlotRole.OUTPUT, x, y)
			.setBackground(getRenderedSlot(), -1, -1)
			.addFluid(stack)
			.addTooltipCallback(CreateRecipeCategory::addPotionTooltip);
	}

	/** Potion fluids get their effect lines right after the name (matches how potion items look). */
	public static void addPotionTooltip(SlotView view, List<Component> tooltip) {
		Optional<FluidStack> displayed = view.getDisplayedFluid();
		if (displayed.isEmpty())
			return;
		FluidStack fluidStack = displayed.get();
		if (fluidStack.getFluid().isSame(AllFluids.POTION.get())) {
			List<Component> potionTooltip = new ArrayList<>();
			PotionFluidHandler.addPotionTooltip(fluidStack, potionTooltip::add, 1);
			tooltip.addAll(Math.min(1, tooltip.size()), potionTooltip.stream().toList());
		}
	}

	public static Icon asIcon(AllGuiTextures texture) {
		return new Icon() {
			@Override
			public int getWidth() {
				return texture.getWidth();
			}

			@Override
			public int getHeight() {
				return texture.getHeight();
			}

			@Override
			public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
				texture.render(graphics, xOffset, yOffset);
			}
		};
	}

	public record Info<T extends Recipe<?>>(ResourceLocation id, Class<? extends T> recipeClass, Component title, Icon background,
											Icon icon, Supplier<List<RecipeHolder<T>>> recipes,
											List<Supplier<? extends ItemStack>> catalysts) {
	}

	public interface Factory<T extends Recipe<?>> {
		CreateRecipeCategory<T> create(Info<T> info);
	}

	public static class Builder<T extends Recipe<? extends RecipeInput>> {
		private final Class<? extends T> recipeClass;
		private Supplier<Boolean> config = () -> true;

		private Icon background;
		private Icon icon;

		private final List<Consumer<List<RecipeHolder<T>>>> recipeListConsumers = new ArrayList<>();
		private final List<Supplier<? extends ItemStack>> catalysts = new ArrayList<>();

		public Builder(Class<? extends T> recipeClass) {
			this.recipeClass = recipeClass;
		}

		public Builder<T> enableWhen(Supplier<Boolean> predicate) {
			this.config = predicate;
			return this;
		}

		public Builder<T> enableWhen(ConfigBool configValue) {
			config = configValue::get;
			return this;
		}

		public Builder<T> addRecipeListConsumer(Consumer<List<RecipeHolder<T>>> consumer) {
			recipeListConsumers.add(consumer);
			return this;
		}

		public Builder<T> addRecipes(Supplier<Collection<? extends RecipeHolder<T>>> collection) {
			return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
		}

		public Builder<T> addAllRecipesIf(Predicate<RecipeHolder<T>> pred) {
			return addRecipeListConsumer(recipes -> consumeAllRecipesOfType(recipe -> {
				if (pred.test(recipe)) recipes.add(recipe);
			}));
		}

		public Builder<T> addAllRecipesIf(Predicate<RecipeHolder<?>> pred, Function<RecipeHolder<?>, RecipeHolder<T>> converter) {
			return addRecipeListConsumer(recipes -> RecipeCollector.consumeAllRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(converter.apply(recipe));
				}
			}));
		}

		public Builder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
			return addTypedRecipes(recipeTypeEntry::getType);
		}

		public <I extends RecipeInput, R extends Recipe<I>> Builder<T> addTypedRecipes(Supplier<net.minecraft.world.item.crafting.RecipeType<R>> recipeType) {
			return addRecipeListConsumer(recipes -> RecipeCollector.<T>consumeTypedRecipes(recipe -> {
				if (recipeClass.isInstance(recipe.value()))
					//noinspection unchecked - checked by if statement above
					recipes.add((RecipeHolder<T>) recipe);
			}, recipeType.get()));
		}

		public Builder<T> addTypedRecipes(Supplier<net.minecraft.world.item.crafting.RecipeType<T>> recipeType, Function<RecipeHolder<?>, RecipeHolder<T>> converter) {
			return addRecipeListConsumer(recipes -> RecipeCollector.<T>consumeTypedRecipes(recipe -> recipes.add(converter.apply(
				recipe)), recipeType.get()));
		}

		public Builder<T> addTypedRecipesIf(Supplier<net.minecraft.world.item.crafting.RecipeType<? extends T>> recipeType, Predicate<RecipeHolder<?>> pred) {
			return addRecipeListConsumer(recipes -> consumeTypedRecipesTyped(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(recipe);
				}
			}, recipeType.get()));
		}

		public Builder<T> addTypedRecipesExcluding(
			Supplier<net.minecraft.world.item.crafting.RecipeType<? extends T>> recipeType, Supplier<net.minecraft.world.item.crafting.RecipeType<? extends T>> excluded
		) {
			return addRecipeListConsumer(recipes -> {
				List<RecipeHolder<?>> excludedRecipes = RecipeCollector.getTypedRecipes(excluded.get());
				consumeTypedRecipesTyped(recipe -> {
					for (RecipeHolder<?> excludedRecipe : excludedRecipes) {
						if (RecipeCollector.doInputsMatch(recipe.value(), excludedRecipe.value())) {
							return;
						}
					}
					recipes.add(recipe);
				}, recipeType.get());
			});
		}

		public Builder<T> removeRecipes(Supplier<net.minecraft.world.item.crafting.RecipeType<? extends T>> recipeType) {
			return addRecipeListConsumer(recipes -> {
				List<RecipeHolder<?>> excludedRecipes = RecipeCollector.getTypedRecipes(recipeType.get());
				recipes.removeIf(recipe -> {
					for (RecipeHolder<?> excludedRecipe : excludedRecipes) {
						if (RecipeCollector.doInputsMatch(recipe.value(), excludedRecipe.value()) &&
							RecipeCollector.doOutputsMatch(recipe.value(), excludedRecipe.value())) return true;
					}
					return false;
				});
			});
		}

		public Builder<T> removeNonAutomation() {
			return addRecipeListConsumer(recipes -> recipes.removeIf(AllRecipeTypes.CAN_BE_AUTOMATED.negate()));
		}

		public Builder<T> catalystStack(Supplier<ItemStack> supplier) {
			catalysts.add(supplier);
			return this;
		}

		public Builder<T> catalyst(Supplier<ItemLike> supplier) {
			return catalystStack(() -> new ItemStack(supplier.get().asItem()));
		}

		public Builder<T> icon(Icon icon) {
			this.icon = icon;
			return this;
		}

		public Builder<T> itemIcon(ItemLike item) {
			icon(new ItemIcon(() -> new ItemStack(item)));
			return this;
		}

		public Builder<T> doubleItemIcon(ItemLike item1, ItemLike item2) {
			icon(new DoubleItemIcon(() -> new ItemStack(item1), () -> new ItemStack(item2)));
			return this;
		}

		public Builder<T> background(Icon background) {
			this.background = background;
			return this;
		}

		public Builder<T> emptyBackground(int width, int height) {
			background(new EmptyBackground(width, height));
			return this;
		}

		public CreateRecipeCategory<T> build(String name, Factory<T> factory) {
			return build(Create.asResource(name), factory);
		}

		public CreateRecipeCategory<T> build(ResourceLocation id, Factory<T> factory) {
			Supplier<List<RecipeHolder<T>>> recipesSupplier;
			if (config.get()) {
				recipesSupplier = () -> {
					List<RecipeHolder<T>> recipes = new ArrayList<>();
					for (Consumer<List<RecipeHolder<T>>> consumer : recipeListConsumers) {consumer.accept(recipes);}
					return recipes;
				};
			} else {
				recipesSupplier = Collections::emptyList;
			}

			Info<T> info = new Info<>(
				id,
				recipeClass,
				Component.translatable(id.getNamespace() + ".recipe." + id.getPath()),
				background,
				icon,
				recipesSupplier,
				catalysts
			);
			return factory.create(info);
		}

		private void consumeAllRecipesOfType(Consumer<RecipeHolder<T>> consumer) {
			RecipeCollector.consumeAllRecipes(recipeHolder -> {
				if (recipeClass.isInstance(recipeHolder.value())) {
					//noinspection unchecked - this is checked by the if statement
					consumer.accept((RecipeHolder<T>) recipeHolder);
				}
			});
		}

		private void consumeTypedRecipesTyped(Consumer<RecipeHolder<T>> consumer, net.minecraft.world.item.crafting.RecipeType<?> type) {
			RecipeCollector.consumeTypedRecipes(recipeHolder -> {
				if (recipeClass.isInstance(recipeHolder.value())) {
					//noinspection unchecked - this is checked by the if statement
					consumer.accept((RecipeHolder<T>) recipeHolder);
				}
			}, type);
		}
	}
}
