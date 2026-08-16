package com.simibubi.create.infrastructure.gametest.tests;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Fabric-port-only tests for the transfer-API bridge ({@code foundation.transfer.capability}): Create inventories must be
 * visible to vanilla hoppers / Fabric storages and vice versa. Built in code on an empty template.
 */
@GameTestGroup(path = "bridge")
public class TestFabricBridge {

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void hopperIntoDepot(CreateGameTestHelper helper) {
		BlockPos depot = new BlockPos(3, 1, 3);
		BlockPos hopper = depot.above();
		helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
		helper.setBlock(hopper, Blocks.HOPPER.defaultBlockState().setValue(BlockStateProperties.FACING_HOPPER, Direction.DOWN));
		HopperBlockEntity be = helper.getBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType.HOPPER, hopper);
		be.setItem(0, new ItemStack(Items.APPLE, 1));
		helper.succeedWhen(() -> {
			helper.assertTrue(ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(depot), Direction.UP) != null, "depot exposes no Fabric item storage");
			helper.assertContainerContains(depot, Items.APPLE);
		});
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void hopperFromDepot(CreateGameTestHelper helper) {
		BlockPos depot = new BlockPos(3, 2, 3);
		BlockPos hopper = depot.below();
		helper.setBlock(hopper, Blocks.HOPPER.defaultBlockState().setValue(BlockStateProperties.FACING_HOPPER, Direction.DOWN));
		helper.setBlock(depot, AllBlocks.DEPOT.getDefaultState());
		helper.getBlockEntity(com.simibubi.create.AllBlockEntityTypes.DEPOT.get(), depot).setHeldItem(new ItemStack(Items.APPLE, 1));
		helper.succeedWhen(() -> helper.assertContainerContains(hopper, Items.APPLE));
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void funnelIntoChest(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(3, 1, 3);
		BlockPos funnel = chest.above();
		helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
		helper.setBlock(funnel, AllBlocks.ANDESITE_FUNNEL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP));
		helper.spawnItem(funnel.above(), new ItemStack(Items.APPLE, 3));
		helper.succeedWhen(() -> helper.assertContainerContains(chest, new ItemStack(Items.APPLE, 3)));
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void createSeesVanillaChest(CreateGameTestHelper helper) {
		BlockPos chest = new BlockPos(3, 1, 3);
		helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
		ChestBlockEntity be = helper.getBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType.CHEST, chest);
		be.setItem(4, new ItemStack(Items.BREAD, 7));
		helper.succeedWhen(() -> {
			var handler = helper.itemStorageAt(chest);
			helper.assertTrue(handler instanceof com.simibubi.create.foundation.transfer.item.IItemHandlerModifiable, "vanilla chest handler is not modifiable");
			helper.assertContainerContains(chest, new ItemStack(Items.BREAD, 7));
		});
	}

	@GameTest(template = "empty", timeoutTicks = 200)
	public static void fluidTankVisibleToFabric(CreateGameTestHelper helper) {
		BlockPos tank = new BlockPos(3, 1, 3);
		helper.setBlock(tank, AllBlocks.FLUID_TANK.getDefaultState());
		helper.succeedWhen(() -> {
			helper.assertTrue(FluidStorage.SIDED.find(helper.getLevel(), helper.absolutePos(tank), Direction.UP) != null, "fluid tank exposes no Fabric fluid storage");
			helper.assertTrue(com.simibubi.create.foundation.transfer.capability.Capabilities.FluidHandler.BLOCK.find(helper.getLevel(), helper.absolutePos(tank), Direction.UP) != null, "fluid tank exposes no fluid handler");
		});
	}

	/** Other Fabric mods' Create integrations use Create Fabric 0.5.x's fluid JSON (droplets, "fluid"/"fluidTag" keys). */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void legacyFabricFluidJson(CreateGameTestHelper helper) {
		com.google.gson.JsonObject stack = com.google.gson.JsonParser.parseString("{\"amount\":81000,\"fluid\":\"minecraft:water\",\"nbt\":{}}").getAsJsonObject();
		com.simibubi.create.foundation.transfer.fluid.FluidStack decoded = com.simibubi.create.foundation.transfer.fluid.FluidStack.DATA_CODEC
			.parse(com.mojang.serialization.JsonOps.INSTANCE, stack).getOrThrow();
		helper.assertTrue(decoded.getFluid() == net.minecraft.world.level.material.Fluids.WATER && decoded.getAmount() == 1000, "legacy stack decoded to " + decoded);
		com.google.gson.JsonObject ing = com.google.gson.JsonParser.parseString("{\"amount\":40500,\"fluidTag\":\"c:milk\"}").getAsJsonObject();
		com.simibubi.create.foundation.transfer.fluid.crafting.SizedFluidIngredient sized = com.simibubi.create.foundation.codec.CreateCodecs.SIZED_FLUID_INGREDIENT
			.parse(com.mojang.serialization.JsonOps.INSTANCE, ing).getOrThrow();
		helper.assertTrue(sized.amount() == 500, "legacy ingredient amount " + sized.amount());
		helper.assertTrue(sized.ingredient().test(new com.simibubi.create.foundation.transfer.fluid.FluidStack(com.simibubi.create.foundation.transfer.fluid.CommonFluids.MILK.get(), 500)), "legacy tag ingredient does not match milk");
		helper.succeed();
	}
}
