package com.simibubi.create.compat.recipeViewer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.recipeViewer.category.*;
import com.simibubi.create.compat.recipeViewer.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.recipeViewer.category.CreateRecipeCategory.Factory;
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
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.data.recipe.LogStrippingFakeRecipes;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;
import com.simibubi.create.foundation.utility.RecipeGenericsUtil;
import com.simibubi.create.infrastructure.config.AllConfigs;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.Holder;
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
import org.jetbrains.annotations.NotNull;

/**
 * Create's recipe-viewer categories, independent of the viewer: the JEI plugin (compat/jei) and the EMI plugin
 * (compat/emi) both build their categories from here. Recipe lists are collected lazily via
 * {@code CreateRecipeCategory#getRecipes()}, so call this once the client has a recipe manager.
 */
@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public final class CreateRecipeCategories {

	private CreateRecipeCategories() {}

	public static List<CreateRecipeCategory<?>> create(IngredientAccess ingredients) {
		List<CreateRecipeCategory<?>> all = new ArrayList<>();
		class CategoryBuilder<T extends Recipe<?>> extends CreateRecipeCategory.Builder<T> {
			CategoryBuilder(Class<? extends T> recipeClass) {
				super(recipeClass);
			}

			@Override
			public CreateRecipeCategory<T> build(ResourceLocation id, Factory<T> factory) {
				CreateRecipeCategory<T> category = super.build(id, factory);
				all.add(category);
				return category;
			}
		}

		CreateRecipeCategory<?>

			milling = new CategoryBuilder<>(AbstractCrushingRecipe.class)
			.addTypedRecipes(AllRecipeTypes.MILLING)
			.catalyst(AllBlocks.MILLSTONE::get)
			.doubleItemIcon(AllBlocks.MILLSTONE.get(), AllItems.WHEAT_FLOUR.get())
			.emptyBackground(177, 53)
			.build("milling", MillingCategory::new),

			crushing = new CategoryBuilder<>(AbstractCrushingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.CRUSHING)
				.addTypedRecipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
				.catalyst(AllBlocks.CRUSHING_WHEEL::get)
				.doubleItemIcon(AllBlocks.CRUSHING_WHEEL.get(), AllItems.CRUSHED_GOLD.get())
				.emptyBackground(177, 100)
				.build("crushing", CrushingCategory::new),

			pressing = new CategoryBuilder<>(PressingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.PRESSING)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), AllItems.IRON_SHEET.get())
				.emptyBackground(177, 70)
				.build("pressing", PressingCategory::new),

			washing = new CategoryBuilder<>(SplashingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SPLASHING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.WATER_BUCKET)
				.emptyBackground(178, 72)
				.build("fan_washing", FanWashingCategory::new),

			smoking = new CategoryBuilder<>(SmokingRecipe.class)
				.addTypedRecipes(() -> RecipeType.SMOKING)
				.removeNonAutomation()
				.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.CAMPFIRE)
				.emptyBackground(178, 72)
				.build("fan_smoking", FanSmokingCategory::new),

			blasting = new CategoryBuilder<>(AbstractCookingRecipe.class)
				.addTypedRecipesExcluding(() -> RecipeType.SMELTING, () -> RecipeType.BLASTING)
				.addTypedRecipes(() -> RecipeType.BLASTING)
				.removeRecipes(() -> RecipeType.SMOKING)
				.removeNonAutomation()
				.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.LAVA_BUCKET)
				.emptyBackground(178, 72)
				.build("fan_blasting", FanBlastingCategory::new),

			haunting = new CategoryBuilder<>(HauntingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.HAUNTING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_haunting"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.SOUL_CAMPFIRE)
				.emptyBackground(178, 72)
				.build("fan_haunting", FanHauntingCategory::new),

			mixing = new CategoryBuilder<>(BasinRecipe.class)
				.addTypedRecipes(AllRecipeTypes.MIXING)
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), AllBlocks.BASIN.get())
				.emptyBackground(177, 103)
				.build("mixing", MixingCategory::standard),

			autoShapeless = new CategoryBuilder<>(BasinRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowShapelessInMixer)
				.addAllRecipesIf(r -> r.value() instanceof CraftingRecipe && !(r.value() instanceof ShapedRecipe)
						&& r.value().getIngredients()
						.size() > 1
						&& !MechanicalPressBlockEntity.canCompress(r.value()) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
					BasinRecipe::convertShapeless)
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), Items.CRAFTING_TABLE)
				.emptyBackground(177, 85)
				.build("automatic_shapeless", MixingCategory::autoShapeless),

			brewing = new CategoryBuilder<>(BasinRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowBrewingInMixer)
				.addRecipes(() -> RecipeGenericsUtil.cast(PotionMixingRecipes.createRecipes(Minecraft.getInstance().level)))
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), Blocks.BREWING_STAND)
				.emptyBackground(177, 103)
				.build("automatic_brewing", MixingCategory::autoBrewing),

			packing = new CategoryBuilder<>(BasinRecipe.class)
				.addTypedRecipes(AllRecipeTypes.COMPACTING)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), AllBlocks.BASIN.get())
				.emptyBackground(177, 103)
				.build("packing", PackingCategory::standard),

			autoSquare = new CategoryBuilder<>(BasinRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowShapedSquareInPress)
				.addAllRecipesIf(
					r -> (r.value() instanceof CraftingRecipe) && !(r.value() instanceof MechanicalCraftingRecipe)
						&& MechanicalPressBlockEntity.canCompress(r.value()) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
					BasinRecipe::convertShapeless)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), Blocks.CRAFTING_TABLE)
				.emptyBackground(177, 85)
				.build("automatic_packing", PackingCategory::autoSquare),

			sawing = new CategoryBuilder<>(CuttingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.CUTTING)
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), Items.OAK_LOG)
				.emptyBackground(177, 70)
				.build("sawing", SawingCategory::new),

			blockCutting = new CategoryBuilder<>(CondensedBlockCuttingRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowStonecuttingOnSaw)
				.addRecipes(() -> BlockCuttingCategory.condenseRecipes(RecipeCollector.getTypedRecipesExcluding(RecipeType.STONECUTTING, AllRecipeTypes::shouldIgnoreInAutomation)))
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), Items.STONE_BRICK_STAIRS)
				.emptyBackground(177, 70)
				.build("block_cutting", BlockCuttingCategory::new),

			polishing = new CategoryBuilder<>(SandPaperPolishingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING)
				.catalyst(AllItems.SAND_PAPER::get)
				.catalyst(AllItems.RED_SAND_PAPER::get)
				.itemIcon(AllItems.SAND_PAPER.get())
				.emptyBackground(177, 55)
				.build("sandpaper_polishing", PolishingCategory::new),

			item_application = new CategoryBuilder<>(ItemApplicationRecipe.class)
				.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION)
				.addRecipes(() -> RecipeGenericsUtil.cast(LogStrippingFakeRecipes.createRecipes()))
				.itemIcon(AllItems.BRASS_HAND.get())
				.emptyBackground(177, 60)
				.build("item_application", ItemApplicationCategory::new),

			deploying = new CategoryBuilder<>(DeployerApplicationRecipe.class)
				.addTypedRecipes(AllRecipeTypes.DEPLOYING)
				.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING::getType, DeployerApplicationRecipe::convert)
				.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION::getType, ManualApplicationRecipe::asDeploying)
				.removeNonAutomation()
				.catalyst(AllBlocks.DEPLOYER::get)
				.catalyst(AllBlocks.DEPOT::get)
				.catalyst(AllItems.BELT_CONNECTOR::get)
				.itemIcon(AllBlocks.DEPLOYER.get())
				.emptyBackground(177, 70)
				.build("deploying", DeployingCategory::new),

			spoutFilling = new CategoryBuilder<>(FillingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.FILLING)
				.addRecipeListConsumer(recipes -> SpoutCategory.consumeRecipes(recipes::add, ingredients))
				.catalyst(AllBlocks.SPOUT::get)
				.doubleItemIcon(AllBlocks.SPOUT.get(), Items.WATER_BUCKET)
				.emptyBackground(177, 70)
				.build("spout_filling", SpoutCategory::new),

			draining = new CategoryBuilder<>(EmptyingRecipe.class)
				.addRecipeListConsumer(recipes -> ItemDrainCategory.consumeRecipes(recipes::add, ingredients))
				.addTypedRecipes(AllRecipeTypes.EMPTYING)
				.catalyst(AllBlocks.ITEM_DRAIN::get)
				.doubleItemIcon(AllBlocks.ITEM_DRAIN.get(), Items.WATER_BUCKET)
				.emptyBackground(177, 50)
				.build("draining", ItemDrainCategory::new),

			autoShaped = new CategoryBuilder<>(CraftingRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowRegularCraftingInCrafter)
				.addAllRecipesIf(r -> r.value() instanceof CraftingRecipe && !(r.value() instanceof ShapedRecipe)
					&& r.value().getIngredients()
					.size() == 1
					&& !AllRecipeTypes.shouldIgnoreInAutomation(r))
				.addTypedRecipesIf(() -> RecipeType.CRAFTING,
					recipe -> recipe.value() instanceof ShapedRecipe && !AllRecipeTypes.shouldIgnoreInAutomation(recipe))
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.itemIcon(AllBlocks.MECHANICAL_CRAFTER.get())
				.emptyBackground(177, 107)
				.build("automatic_shaped", MechanicalCraftingCategory::new),

			mechanicalCrafting = new CategoryBuilder<>(CraftingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.MECHANICAL_CRAFTING)
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.itemIcon(AllBlocks.MECHANICAL_CRAFTER.get())
				.emptyBackground(177, 107)
				.build("mechanical_crafting", MechanicalCraftingCategory::new),

			seqAssembly = new CategoryBuilder<>(SequencedAssemblyRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SEQUENCED_ASSEMBLY)
				.itemIcon(AllItems.PRECISION_MECHANISM.get())
				.emptyBackground(180, 115)
				.build("sequenced_assembly", SequencedAssemblyCategory::new),

			mysteryConversion = new CategoryBuilder<>(ConversionRecipe.class)
				.addRecipes(() -> MysteriousItemConversionCategory.RECIPES)
				.itemIcon(AllBlocks.PECULIAR_BELL.get())
				.emptyBackground(177, 50)
				.build("mystery_conversion", MysteriousItemConversionCategory::new);

		return all;
	}
}
