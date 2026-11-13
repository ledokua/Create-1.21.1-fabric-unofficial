package com.simibubi.create.infrastructure.gametest.tests;

import java.util.List;
import java.util.function.BiPredicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.transfer.capability.bridge.StorageItemHandler;
import com.simibubi.create.foundation.transfer.item.IItemHandler;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Fabric-port-only tests for port fixes that have no upstream test (6.0.11-fabric.9). */
@GameTestGroup(path = "portfixes")
public class TestFabricPortFixes {

	/**
	 * The cached recipe attributes ("can be smelted", ...) answer exactly like a fresh recipe lookup, for every item,
	 * on the first (uncached) and the second (cached) call.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void recipeAttributeCacheMatchesRecipes(CreateGameTestHelper helper) {
		Level level = helper.getLevel();
		record Case(String name, ItemAttributeType type, BiPredicate<ItemStack, Level> direct) {}
		List<Case> cases = List.of(
			new Case("smeltable", AllItemAttributeTypes.SMELTABLE, (s, w) -> lookup(s, w, RecipeType.SMELTING)),
			new Case("smokable", AllItemAttributeTypes.SMOKABLE, (s, w) -> lookup(s, w, RecipeType.SMOKING)),
			new Case("blastable", AllItemAttributeTypes.BLASTABLE, (s, w) -> lookup(s, w, RecipeType.BLASTING)),
			new Case("crushable", AllItemAttributeTypes.CRUSHABLE, (s, w) -> lookup(s, w, AllRecipeTypes.CRUSHING.getType())
				|| lookup(s, w, AllRecipeTypes.MILLING.getType())),
			new Case("washable", AllItemAttributeTypes.WASHABLE, AllFanProcessingTypes.SPLASHING::canProcess),
			new Case("hauntable", AllItemAttributeTypes.HAUNTABLE, AllFanProcessingTypes.HAUNTING::canProcess));

		List<ItemStack> stacks = BuiltInRegistries.ITEM.stream()
			.filter(i -> i != Items.AIR)
			.map(ItemStack::new)
			.toList();
		for (Case c : cases) {
			ItemAttribute attribute = c.type().createAttribute();
			int positives = 0;
			long directNanos = 0, cachedNanos = 0;
			for (ItemStack stack : stacks) {
				long t0 = System.nanoTime();
				boolean expected = c.direct().test(stack, level);
				long t1 = System.nanoTime();
				boolean first = attribute.appliesTo(stack, level);
				long t2 = System.nanoTime();
				boolean second = attribute.appliesTo(stack.copyWithCount(stack.getMaxStackSize()), level);
				long t3 = System.nanoTime();
				directNanos += t1 - t0;
				cachedNanos += t3 - t2;
				helper.assertTrue(first == expected && second == expected,
					c.name() + " for " + BuiltInRegistries.ITEM.getKey(stack.getItem()) + ": lookup " + expected + ", attribute " + first + "/" + second);
				if (expected)
					positives++;
			}
			Create.LOGGER.info("[portfixes] {}: {} of {} items, recipe lookups {} ms, cached {} ms", c.name(), positives, stacks.size(),
				directNanos / 1_000_000, cachedNanos / 1_000_000);
			helper.assertTrue(positives > 0, c.name() + " matched no item at all");
		}
		helper.succeed();
	}

	private static <T extends net.minecraft.world.item.crafting.Recipe<SingleRecipeInput>> boolean lookup(ItemStack s, Level w, RecipeType<T> type) {
		return w.getRecipeManager().getRecipeFor(type, new SingleRecipeInput(s.copy()), w).isPresent();
	}

	/** Drills leave budding amethyst alone by default (config drillsBreakBuddingAmethyst), but still harvest clusters. */
	@GameTest(template = "empty", timeoutTicks = 260)
	public static void drillsKeepBuddingAmethyst(CreateGameTestHelper helper) {
		BlockPos budding = new BlockPos(3, 1, 2), cluster = new BlockPos(3, 1, 4);
		for (BlockPos target : new BlockPos[] { budding, cluster }) {
			BlockPos drill = target.west(), motor = drill.west();
			helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
			helper.setBlock(drill, AllBlocks.MECHANICAL_DRILL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
		}
		helper.setBlock(budding, Blocks.BUDDING_AMETHYST.defaultBlockState());
		helper.setBlock(cluster, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
		helper.runAfterDelay(240, () -> {
			helper.assertBlockPresent(Blocks.BUDDING_AMETHYST, budding);
			helper.assertBlockNotPresent(Blocks.AMETHYST_CLUSTER, cluster);
			helper.succeed();
		});
	}

	/** The item bridge reports a bulk slot's real capacity (threshold switch on drawers), while getSlotLimit stays a stack. */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void bridgeReportsBulkSlotCapacity(CreateGameTestHelper helper) {
		SingleVariantStorage<ItemVariant> bulk = new SingleVariantStorage<>() {
			@Override
			protected ItemVariant getBlankVariant() {
				return ItemVariant.blank();
			}

			@Override
			protected long getCapacity(ItemVariant variant) {
				return 4096;
			}
		};
		bulk.variant = ItemVariant.of(Items.IRON_INGOT);
		bulk.amount = 500;
		IItemHandler handler = StorageItemHandler.of(bulk);
		helper.assertTrue(handler instanceof StorageItemHandler, "bridge returned " + handler.getClass().getSimpleName());
		StorageItemHandler storageHandler = (StorageItemHandler) handler;
		helper.assertTrue(storageHandler.getSlotCapacity(0) == 4096, "capacity " + storageHandler.getSlotCapacity(0));
		helper.assertTrue(handler.getStackInSlot(0).getCount() == 500, "count " + handler.getStackInSlot(0).getCount());
		helper.assertTrue(handler.getSlotLimit(0) == 64, "slot limit " + handler.getSlotLimit(0));

		// vanilla containers are wrapped directly (InvWrapper), so the threshold switch keeps its old per-stack math for them
		BlockPos chest = new BlockPos(2, 1, 2);
		helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
		IItemHandler chestHandler = helper.itemStorageAt(chest);
		helper.assertTrue(!(chestHandler instanceof StorageItemHandler), "chest handler " + chestHandler.getClass().getSimpleName());
		helper.succeed();
	}
}
