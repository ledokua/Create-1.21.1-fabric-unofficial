package com.simibubi.create.infrastructure.gametest.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.simibubi.create.api.contraption.storage.item.simple.SimpleMountedStorage;
import com.simibubi.create.content.contraptions.minecart.TrainCargoManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.transfer.item.ItemStackHandler;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Fabric-port-only tests for port fixes that have no upstream test (6.0.11-fabric.9+). */
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

	/**
	 * Stationary drills leave budding amethyst and small/medium/large amethyst buds alone by default (configs
	 * drillsBreakBuddingAmethyst, drillsBreakAmethystBuds), but still harvest a grown cluster.
	 */
	@GameTest(template = "empty", timeoutTicks = 260)
	public static void drillsKeepBuddingAmethystAndBuds(CreateGameTestHelper helper) {
		net.minecraft.world.level.block.Block[] kept = { Blocks.BUDDING_AMETHYST, Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD,
			Blocks.LARGE_AMETHYST_BUD };
		for (int z = 0; z <= kept.length; z++) {
			BlockPos target = new BlockPos(3, 1, z), drill = target.west(), motor = drill.west();
			helper.setBlock(target.east(), Blocks.BEDROCK.defaultBlockState());
			helper.setBlock(motor, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
			helper.setBlock(drill, AllBlocks.MECHANICAL_DRILL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
			net.minecraft.world.level.block.Block block = z < kept.length ? kept[z] : Blocks.AMETHYST_CLUSTER;
			helper.setBlock(target, block == Blocks.BUDDING_AMETHYST ? block.defaultBlockState()
				: block.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST));
		}
		helper.runAfterDelay(240, () -> {
			for (int z = 0; z < kept.length; z++)
				helper.assertBlockPresent(kept[z], new BlockPos(3, 1, z));
			helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 1, kept.length));
			helper.succeed();
		});
	}

	/**
	 * A gantry sweeps an upward drill under a bedrock ceiling with small/medium/large buds and a cluster hanging from it,
	 * plus a small bud right in the drill's path. The cluster is harvested; the buds stay, and the one in the path is
	 * passed over instead of stopping the gantry or being broken.
	 */
	@GameTest(template = "lane", timeoutTicks = 900)
	public static void movingDrillSkipsAmethystBuds(CreateGameTestHelper helper) {
		int x = 3;
		helper.setBlock(new BlockPos(x, 1, 0), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.SOUTH));
		for (int z = 1; z <= 14; z++)
			helper.setBlock(new BlockPos(x, 1, z), AllBlocks.GANTRY_SHAFT.getDefaultState().setValue(BlockStateProperties.FACING, Direction.SOUTH));
		for (int z = 1; z <= 14; z++) {
			BlockPos rel = new BlockPos(x, 1, z), abs = helper.absolutePos(rel);
			helper.getLevel().setBlockAndUpdate(abs, net.minecraft.world.level.block.Block.updateFromNeighbourShapes(helper.getLevel().getBlockState(abs), helper.getLevel(), abs));
		}
		for (int z = 1; z <= 12; z++)
			helper.setBlock(new BlockPos(x, 5, z), Blocks.BEDROCK.defaultBlockState());
		BlockPos small = new BlockPos(x, 4, 10), medium = new BlockPos(x, 4, 8), large = new BlockPos(x, 4, 6), cluster = new BlockPos(x, 4, 4);
		BlockPos inPathSupport = new BlockPos(x, 4, 11), inPath = new BlockPos(x, 3, 11);
		helper.setBlock(small, Blocks.SMALL_AMETHYST_BUD.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
		helper.setBlock(medium, Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
		helper.setBlock(large, Blocks.LARGE_AMETHYST_BUD.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
		helper.setBlock(cluster, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
		helper.setBlock(inPathSupport, Blocks.BEDROCK.defaultBlockState());
		helper.setBlock(inPath, Blocks.SMALL_AMETHYST_BUD.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN));
		helper.setBlock(new BlockPos(x, 3, 13), AllBlocks.MECHANICAL_DRILL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP));
		helper.setBlock(new BlockPos(x, 2, 13), AllBlocks.GANTRY_CARRIAGE.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP)
			.setValue(com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, true));
		helper.getBlockEntity(com.simibubi.create.AllBlockEntityTypes.MOTOR.get(), new BlockPos(x, 1, 0)).generatedSpeed.setValue(128);
		helper.succeedWhen(() -> {
			helper.assertBlockPresent(AllBlocks.GANTRY_CARRIAGE.get(), new BlockPos(x, 2, 1));
			helper.assertBlockPresent(AllBlocks.MECHANICAL_DRILL.get(), new BlockPos(x, 3, 1));
			helper.assertBlockPresent(Blocks.SMALL_AMETHYST_BUD, small);
			helper.assertBlockPresent(Blocks.MEDIUM_AMETHYST_BUD, medium);
			helper.assertBlockPresent(Blocks.LARGE_AMETHYST_BUD, large);
			helper.assertBlockPresent(Blocks.SMALL_AMETHYST_BUD, inPath);
			helper.assertBlockPresent(Blocks.AIR, cluster);
		});
	}

	private static void placeLink(CreateGameTestHelper helper, BlockPos pos, boolean receiver) {
		helper.setBlock(pos.below(), Blocks.STONE.defaultBlockState());
		helper.setBlock(pos, AllBlocks.REDSTONE_LINK.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP)
			.setValue(com.simibubi.create.content.redstone.link.RedstoneLinkBlock.RECEIVER, receiver));
		// placed with setBlock, the link has no LinkBehaviour until its block entity reads data
		var be = helper.getBlockEntity(com.simibubi.create.AllBlockEntityTypes.REDSTONE_LINK.get(), pos);
		var registries = helper.getLevel().registryAccess();
		be.loadWithComponents(be.saveWithFullMetadata(registries), registries);
	}

	/**
	 * Idle transmitters side by side stop scheduling each other's tick (fabric.12; before, each one's neighbour updates
	 * re-scheduled the other every tick, forever).
	 */
	@GameTest(template = "empty", timeoutTicks = 100)
	public static void idleLinkTransmittersSettle(CreateGameTestHelper helper) {
		BlockPos a = new BlockPos(2, 2, 3), b = new BlockPos(3, 2, 3);
		placeLink(helper, a, false);
		placeLink(helper, b, false);
		int[] scheduled = new int[1];
		int[] ticks = new int[1];
		helper.onEachTick(() -> {
			ticks[0]++;
			if (ticks[0] < 20)
				return;
			for (BlockPos pos : new BlockPos[] { a, b })
				if (helper.getLevel().getBlockTicks().hasScheduledTick(helper.absolutePos(pos), AllBlocks.REDSTONE_LINK.get()))
					scheduled[0]++;
		});
		helper.runAfterDelay(80, () -> {
			Create.LOGGER.info("[portfixes] idle transmitters side by side: {} scheduled ticks seen in 60 ticks", scheduled[0]);
			helper.assertTrue(scheduled[0] == 0, scheduled[0] + " link ticks scheduled while idle");
			helper.succeed();
		});
	}

	/** A transmitter still sends a redstone signal to a receiver on the same frequency, and stops when it's removed. */
	@GameTest(template = "empty", timeoutTicks = 100)
	public static void linkTransmitsSignal(CreateGameTestHelper helper) {
		BlockPos transmitter = new BlockPos(1, 2, 3), source = new BlockPos(1, 2, 2), receiver = new BlockPos(5, 2, 3);
		placeLink(helper, transmitter, false);
		placeLink(helper, receiver, true);
		helper.runAfterDelay(10, () -> helper.setBlock(source, Blocks.REDSTONE_BLOCK.defaultBlockState()));
		helper.runAfterDelay(30, () -> {
			helper.assertBlockProperty(receiver, com.simibubi.create.content.redstone.link.RedstoneLinkBlock.POWERED, true);
			helper.setBlock(source, Blocks.AIR.defaultBlockState());
		});
		helper.runAfterDelay(50, () -> {
			helper.assertBlockProperty(receiver, com.simibubi.create.content.redstone.link.RedstoneLinkBlock.POWERED, false);
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

	/**
	 * A driven train without fuel no longer scans its fuel slots every tick (Fabric's fuel lookup is expensive), but
	 * still picks up fuel right away when its cargo changes.
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void trainWithoutFuelScansOncePerSecond(CreateGameTestHelper helper) {
		int[] scans = {0};
		int[] version = {0};
		MountedItemStorageWrapper fuel = new MountedItemStorageWrapper(
			ImmutableMap.of(BlockPos.ZERO, new SimpleMountedStorage(new ItemStackHandler(27)))) {
			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				if (simulate && slot == 0)
					scans[0]++;
				return super.extractItem(slot, amount, simulate);
			}
		};
		for (int slot = 0; slot < fuel.getSlots(); slot++)
			fuel.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
		TrainCargoManager cargo = new TrainCargoManager() {
			@Override
			public MountedItemStorageWrapper getFuelItems() {
				return fuel;
			}

			@Override
			public int getVersion() {
				return version[0];
			}
		};
		Carriage carriage = new Carriage(new CarriageBogey(AllBlocks.SMALL_BOGEY.get(), false, new CompoundTag()), null, 0);
		carriage.storage = cargo;
		Train train = new Train(UUID.randomUUID(), UUID.randomUUID(), null, new ArrayList<>(List.of(carriage)),
			new ArrayList<>(), false, 0);

		for (int tick = 0; tick < 60; tick++)
			train.burnFuel();
		helper.assertTrue(scans[0] == 3, "fuel scans in 60 ticks without fuel: " + scans[0]);
		helper.assertTrue(train.fuelTicks == 0, "fuel ticks " + train.fuelTicks);

		// fuel loaded by a station or funnel bumps the cargo version: picked up on the next tick
		fuel.setStackInSlot(5, new ItemStack(Items.COAL, 2));
		version[0]++;
		train.burnFuel();
		helper.assertTrue(train.fuelTicks == 1600, "fuel ticks after loading coal " + train.fuelTicks);
		helper.assertTrue(fuel.getStackInSlot(5).getCount() == 1, "coal left " + fuel.getStackInSlot(5).getCount());

		// fuel put in without a version change (e.g. a player in a mounted chest) is found within a second
		fuel.setStackInSlot(5, ItemStack.EMPTY);
		train.fuelTicks = 1;
		train.burnFuel();
		train.burnFuel();
		fuel.setStackInSlot(7, new ItemStack(Items.COAL));
		int waited = 0;
		while (train.fuelTicks == 0 && waited < 40) {
			train.burnFuel();
			waited++;
		}
		helper.assertTrue(train.fuelTicks > 0 && waited <= 21, "coal found after " + waited + " ticks");
		helper.succeed();
	}

	/**
	 * Two players linked to the same toolbox compartment, both with the toolbox's screen open and an empty hotbar slot:
	 * the toolbox unequips them while it iterates its player map. That threw a ConcurrentModificationException and
	 * crashed the live server (6.0.11-fabric.13, "Ticking block entity" in ToolboxBlockEntity.tickPlayers).
	 */
	@GameTest(template = "empty", timeoutTicks = 20)
	public static void toolboxUnequipsSeveralPlayersWithoutCrash(CreateGameTestHelper helper) {
		BlockPos boxPos = new BlockPos(3, 1, 3);
		helper.setBlock(boxPos, AllBlocks.TOOLBOXES.get(net.minecraft.world.item.DyeColor.BROWN).getDefaultState());
		var box = helper.getBlockEntity(com.simibubi.create.AllBlockEntityTypes.TOOLBOX.get(), boxPos);
		IItemHandler boxInventory = helper.itemStorageAt(boxPos);
		boxInventory.insertItem(0, new ItemStack(Items.COBBLESTONE, 64), false);
		helper.assertTrue(boxInventory.getStackInSlot(0).getCount() == 64, "toolbox holds " + boxInventory.getStackInSlot(0));

		List<net.minecraft.server.level.ServerPlayer> players = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			net.minecraft.server.level.ServerPlayer player = helper.makeMockServerPlayerInLevel();
			BlockPos abs = helper.absolutePos(boxPos.east());
			player.setPos(abs.getX() + .5, abs.getY(), abs.getZ() + .5);
			net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
			data.putInt("Slot", 0);
			data.put("Pos", net.minecraft.nbt.NbtUtils.writeBlockPos(box.getBlockPos()));
			net.minecraft.nbt.CompoundTag toolboxData = new net.minecraft.nbt.CompoundTag();
			toolboxData.put("0", data);
			player.getPersistentData().put("CreateToolboxData", toolboxData);
			box.connectPlayer(0, player, 0);
			player.containerMenu = com.simibubi.create.content.equipment.toolbox.ToolboxMenu.create(100 + i, player.getInventory(), box);
			players.add(player);
		}

		try {
			box.tick();
		} catch (java.util.ConcurrentModificationException e) {
			helper.fail("toolbox tick threw " + e);
		}
		for (net.minecraft.server.level.ServerPlayer player : players)
			helper.assertTrue(!player.getPersistentData().getCompound("CreateToolboxData").contains("0"),
				"a player is still linked to the open toolbox");
		players.forEach(p -> p.discard());
		helper.succeed();
	}

	/**
	 * Stock ticker orders for toolboxes: the order holds the client's copy of the item (decoded from the network). A
	 * toolbox picked up in-game and one loaded from disk with a compartment filter are both found in the packager's
	 * stock and packed. Before 6.0.11-fabric.15 neither was ever packed.
	 */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void stockOrderPacksToolboxes(CreateGameTestHelper helper) {
		var registries = helper.getLevel().registryAccess();
		BlockPos boxPos = new BlockPos(5, 1, 5);
		helper.setBlock(boxPos, AllBlocks.TOOLBOXES.get(net.minecraft.world.item.DyeColor.BROWN).getDefaultState());
		helper.itemStorageAt(boxPos).insertItem(0, new ItemStack(Items.COBBLESTONE, 64), false);
		var boxBE = helper.getBlockEntity(com.simibubi.create.AllBlockEntityTypes.TOOLBOX.get(), boxPos);
		// picked up in-game: the item carries the toolbox's live inventory
		ItemStack pickedUp = new ItemStack(AllBlocks.TOOLBOXES.get(net.minecraft.world.item.DyeColor.BROWN).get());
		pickedUp.applyComponents(boxBE.collectComponents());
		// after a restart: read from disk
		ItemStack fromDisk = ItemStack.parseOptional(registries, (net.minecraft.nbt.CompoundTag) pickedUp.save(registries));
		helper.assertTrue(pickedUp.has(com.simibubi.create.AllDataComponents.TOOLBOX_INVENTORY), "toolbox item has no inventory component");

		List<ItemStack> toolboxes = List.of(pickedUp, fromDisk);
		for (int n = 0; n < 2; n++) {
			BlockPos chest = new BlockPos(1 + 2 * n, 1, 2);
			helper.setBlock(chest, Blocks.CHEST.defaultBlockState());
			helper.setBlock(chest.above(), com.simibubi.create.AllBlocks.PACKAGER.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP));
			helper.itemStorageAt(chest).insertItem(0, toolboxes.get(n).copy(), false);
		}
		helper.runAfterDelay(5, () -> {
			for (int n = 0; n < 2; n++) {
				String name = n == 0 ? "picked-up toolbox" : "toolbox from disk";
				BlockPos chest = new BlockPos(1 + 2 * n, 1, 2);
				var packager = helper.getBlockEntity(com.simibubi.create.AllBlockEntityTypes.PACKAGER.get(), chest.above());
				io.netty.buffer.ByteBuf raw = io.netty.buffer.Unpooled.buffer();
				net.minecraft.network.RegistryFriendlyByteBuf buf = new net.minecraft.network.RegistryFriendlyByteBuf(raw, registries);
				ItemStack.STREAM_CODEC.encode(buf, toolboxes.get(n));
				ItemStack ordered = ItemStack.STREAM_CODEC.decode(buf);

				helper.assertTrue(packager.getAvailableItems().getCountOfMatching(ordered) == 1, name + ": not in the packager's stock ("
					+ packager.getAvailableItems().getCountOf(toolboxes.get(n)) + " of the original)");
				List<com.simibubi.create.content.logistics.packager.PackagingRequest> queue = new ArrayList<>();
				queue.add(com.simibubi.create.content.logistics.packager.PackagingRequest.create(ordered, 1, "test", 0,
					new org.apache.commons.lang3.mutable.MutableBoolean(true), 0, 1, null));
				packager.attemptToSend(queue);
				helper.assertTrue(!packager.heldBox.isEmpty(), name + ": not packed");
				helper.assertTrue(helper.itemStorageAt(chest).getStackInSlot(0).isEmpty(), name + ": still in the chest");
			}
			helper.succeed();
		});
	}

	/**
	 * A mob on a belt keeps Slowness II every tick while the belt only refreshes it when it's about to run out (it was
	 * re-applied every tick before 6.0.11-fabric.16).
	 */
	@GameTest(template = "empty", timeoutTicks = 60)
	public static void beltLockRefreshesSlownessRarely(CreateGameTestHelper helper) {
		var pig = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(3, 1, 3));
		pig.setNoAi(true);
		int[] applied = {0};
		for (int t = 1; t <= 40; t++) {
			int tick = t;
			helper.runAfterDelay(t, () -> {
				if (tick > 1) {
					var effect = pig.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
					helper.assertTrue(effect != null && effect.getAmplifier() >= 1, "pig not slowed at tick " + tick);
				}
				if (com.simibubi.create.content.kinetics.belt.transport.BeltMovementHandler.needsBeltLock(pig)) {
					pig.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 10, 1, false, false));
					applied[0]++;
				}
			});
		}
		helper.runAfterDelay(42, () -> {
			helper.assertTrue(applied[0] <= 10, "slowness applied " + applied[0] + " times in 40 ticks");
			helper.succeed();
		});
	}

	/**
	 * Diving boots still pull a mob down in water; mobs without boots on land get no persistent data and no marker work.
	 */
	@GameTest(template = "empty", timeoutTicks = 60)
	public static void divingBootsCheckSkipsDryMobs(CreateGameTestHelper helper) {
		for (int x = 1; x <= 3; x++)
			for (int z = 1; z <= 3; z++)
				for (int y = 1; y <= 4; y++)
					helper.setBlock(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState());
		var diver = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(2, 4, 2));
		diver.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, com.simibubi.create.AllItems.COPPER_DIVING_BOOTS.asStack());
		var swimmer = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(3, 4, 3));
		var dry = helper.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(5, 1, 5));
		dry.setNoAi(true);
		helper.runAfterDelay(20, () -> {
			var holder = (com.simibubi.create.foundation.extension.EntityExtension.PersistentDataHolder) dry;
			helper.assertTrue(holder.create$persistentDataIfPresent() == null, "dry pig got persistent data");
			helper.assertTrue(diver.getPersistentData().contains("HeavyBoots"), "zombie with diving boots in water has no HeavyBoots marker");
			helper.assertTrue(!swimmer.getPersistentData().contains("HeavyBoots"), "zombie without boots has the marker");
			helper.succeed();
		});
		helper.runAfterDelay(6, () -> helper.assertTrue(diver.getY() < swimmer.getY() - 0.3,
			"boots don't pull the zombie down: " + diver.getY() + " vs " + swimmer.getY()));
	}
}
