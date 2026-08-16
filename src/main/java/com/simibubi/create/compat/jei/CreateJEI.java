package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.data.recipe.LogStrippingFakeRecipes;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.RecipeGenericsUtil;
import com.simibubi.create.infrastructure.config.AllConfigs;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import com.simibubi.create.compat.recipeViewer.CreateRecipeCategories;
import com.simibubi.create.compat.recipeViewer.RecipeCollector;
import com.simibubi.create.compat.recipeViewer.ToolboxColoringRecipeMaker;
import com.simibubi.create.compat.recipeViewer.category.CreateRecipeCategory;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CreateJEI implements IModPlugin {

	private static final ResourceLocation ID = Create.asResource("jei_plugin");

	private final List<JeiCategoryAdapter<?>> allCategories = new ArrayList<>();
	private IIngredientManager ingredientManager;

	public static IJeiRuntime runtime;

	private void loadCategories() {
		allCategories.clear();
		for (CreateRecipeCategory<?> category : CreateRecipeCategories.create(new JeiIngredientAccess(() -> ingredientManager)))
			allCategories.add(new JeiCategoryAdapter<>(category));
	}

	@Override
	@NotNull
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		loadCategories();
		registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ingredientManager = registration.getIngredientManager();

		allCategories.forEach(c -> registerRecipes(registration, c));

		registration.addRecipes(RecipeTypes.CRAFTING, ToolboxColoringRecipeMaker.createRecipes().toList());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		allCategories.forEach(c -> c.category.getCatalysts().forEach(s -> registration.addRecipeCatalyst(s.get(), c.type)));
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(new BlueprintTransferHandler(), RecipeTypes.CRAFTING);
		registration.addUniversalRecipeTransferHandler(new StockKeeperTransferHandler(registration.getJeiHelpers()));
	}

	@Override
	public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		PotionFluidSubtypeInterpreter interpreter = new PotionFluidSubtypeInterpreter();
		PotionFluid potionFluid = AllFluids.POTION.get();
		registration.registerSubtypeInterpreter(CreateJeiIngredientTypes.FLUID_STACK, potionFluid.getSource(), interpreter);
		registration.registerSubtypeInterpreter(CreateJeiIngredientTypes.FLUID_STACK, potionFluid.getFlowing(), interpreter);
	}

	@Override
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
		List<Reference<Potion>> potions = registryAccess.lookupOrThrow(Registries.POTION)
			.listElements()
			.toList();
		Collection<FluidStack> potionFluids = new ArrayList<>(potions.size() * 3);
		Set<Set<Holder<MobEffect>>> visitedEffects = new HashSet<>();
		for (Reference<Potion> potion : potions) {
			// @goshante: Ingame potion fluids always have Bottle tag that specifies
			// to what bottle type this potion belongs
			// Potion fluid without this tag wouldn't be recognized by other mods

//			for (PotionFluid.BottleType bottleType : PotionFluid.BottleType.values()) {
//				FluidStack potionFluid = PotionFluid.of(1000, new PotionContents(potion), bottleType);
//				potionFluids.add(potionFluid);
//			}

			PotionContents potionContents = new PotionContents(potion);

			if (potionContents.hasEffects()) {
				Set<Holder<MobEffect>> effectSet = new HashSet<>();
				potionContents.forEachEffect(mei -> effectSet.add(mei.getEffect()));
				if (!visitedEffects.add(effectSet))
					continue;
}

			potionFluids.add(PotionFluid.of(1000, potionContents, PotionFluid.BottleType.REGULAR));
		}
		registration.addExtraIngredients(CreateJeiIngredientTypes.FLUID_STACK, CreateJeiIngredientTypes.toJei(potionFluids));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());

		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(LinkedControllerScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(ScheduleScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(RedstoneRequesterScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(FactoryPanelSetItemScreen.class, new GhostIngredientHandler());

		registration.addGuiContainerHandler(StockKeeperRequestScreen.class, new StockKeeperGuiContainerHandler(ingredientManager));
	}

	private static <T extends Recipe<?>> void registerRecipes(IRecipeRegistration registration, JeiCategoryAdapter<T> adapter) {
		registration.addRecipes(adapter.type, adapter.category.getRecipes());
	}

	/** Kept for callers outside this package. */
	public static void consumeAllRecipes(Consumer<? super RecipeHolder<?>> consumer) {
		RecipeCollector.consumeAllRecipes(consumer);
	}

	public static <T extends Recipe<?>> void consumeTypedRecipes(Consumer<RecipeHolder<?>> consumer, RecipeType<?> type) {
		RecipeCollector.consumeTypedRecipes(consumer, type);
	}

	public static List<RecipeHolder<?>> getTypedRecipes(RecipeType<?> type) {
		return RecipeCollector.getTypedRecipes(type);
	}

	public static List<RecipeHolder<?>> getTypedRecipesExcluding(RecipeType<?> type, Predicate<RecipeHolder<?>> exclusionPred) {
		return RecipeCollector.getTypedRecipesExcluding(type, exclusionPred);
	}

	public static boolean doInputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		return RecipeCollector.doInputsMatch(recipe1, recipe2);
	}

	public static boolean doOutputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		return RecipeCollector.doOutputsMatch(recipe1, recipe2);
	}

	/** JEI's ingredient manager as the neutral IngredientAccess. */
	private record JeiIngredientAccess(java.util.function.Supplier<IIngredientManager> manager) implements com.simibubi.create.compat.recipeViewer.IngredientAccess {
		@Override
		public java.util.Collection<ItemStack> allItems() {
			return manager.get().getAllIngredients(VanillaTypes.ITEM_STACK);
		}

		@Override
		public java.util.Collection<FluidStack> allFluids() {
			return manager.get().getAllIngredients(CreateJeiIngredientTypes.FLUID_STACK).stream().map(CreateJeiIngredientTypes::fromJei).toList();
		}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		CreateJEI.runtime = runtime;
	}

}
