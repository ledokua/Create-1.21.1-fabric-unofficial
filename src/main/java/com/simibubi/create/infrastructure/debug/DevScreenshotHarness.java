package com.simibubi.create.infrastructure.debug;

import java.io.File;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.foundation.transfer.capability.Capabilities;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;
import com.simibubi.create.foundation.transfer.fluid.IFluidHandler;

import net.createmod.ponder.foundation.ui.PonderUI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

/**
 * Dev-only rendering smoke test (no equivalent of Fabric's client gametests exists for 1.21.1): with
 * {@code -Dcreate.harness=showcase} the client, once in a world, builds a showcase of Create blocks in the air, teleports
 * the player in front of it, saves screenshots to {@code <run>/screenshots/create-harness-*.png} (overview, goggles
 * overlay, Ponder UI) and quits. Purely a porting aid; inert without the property.
 */
@Environment(EnvType.CLIENT)
public final class DevScreenshotHarness {

	private DevScreenshotHarness() {}

	private static final BlockPos BASE = new BlockPos(0, 150, 0);
	private static int tick = -1;
	private static int phase = 0;
	private static boolean done;

	public static void init() {
		if (!"showcase".equals(System.getProperty("create.harness", System.getenv("CREATE_HARNESS"))))
			return;
		Create.LOGGER.info("[harness] screenshot harness armed");
		ClientTickEvents.END_CLIENT_TICK.register(DevScreenshotHarness::tick);
	}

	private static void tick(Minecraft mc) {
		if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
			return;
		mc.options.pauseOnLostFocus = false;
		tick++;
		try {
			switch (phase) {
				case 0 -> { if (tick >= 40) { build(mc); probeBuffers(mc); probeItemModels(mc); phase = 1; tick = 0; } }
				case 1 -> { if (tick == 5) mc.player.getInventory().selected = 0; if (tick >= 140) { screenshot(mc, "overview"); teleport(mc, 5.5, 152.0, -1.5, 0, 46, false); phase = 2; tick = 0; } }
				case 2 -> { if (tick >= 60) { screenshot(mc, "goggles"); teleport(mc, 22.5, 154.5, 3.5, 0, 35, false); phase = 5; tick = 0; } }
				case 5 -> { if (tick >= 60) { screenshot(mc, "contraption"); openCreativeTab(mc, com.simibubi.create.AllCreativeModeTabs.BASE_CREATIVE_TAB.get()); phase = 6; tick = 0; } }
				case 6 -> { if (tick >= 30) { screenshot(mc, "creative-base"); openCreativeTab(mc, com.simibubi.create.AllCreativeModeTabs.PALETTES_CREATIVE_TAB.get()); phase = 7; tick = 0; } }
				case 7 -> { if (tick >= 30) { screenshot(mc, "creative-palettes"); mc.setScreen(new net.createmod.catnip.config.ui.BaseConfigScreen(null, Create.ID)); phase = 8; tick = 0; } }
				case 8 -> { if (tick >= 30) { screenshot(mc, "config"); mc.setScreen(PonderUI.of(ponderTarget())); phase = 3; tick = 0; } }
				case 3 -> {
					if (tick == 60) screenshot(mc, "ponder-1");
					if (tick == 160) screenshot(mc, "ponder-2");
					if (tick == 260) screenshot(mc, "ponder-3");
					if (tick >= 360) { screenshot(mc, "ponder-4"); mc.setScreen(null); phase = 4; tick = 0; }
				}
				case 4 -> { if (tick >= 5) { Create.LOGGER.info("[harness] done"); done = true; mc.stop(); } }
			}
		} catch (Throwable t) {
			Create.LOGGER.error("[harness] failed", t);
			done = true;
			mc.stop();
		}
	}

	/** Logs every Create item whose baked model is empty (no quads, no custom renderer) — i.e. would render invisible. */
	private static void probeItemModels(Minecraft mc) {
		int invisible = 0, total = 0;
		var missing = mc.getModelManager().getMissingModel();
		for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
			if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Create.ID))
				continue;
			total++;
			ItemStack stack = new ItemStack(item);
			var model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
			int quads = model.getQuads(null, null, net.minecraft.util.RandomSource.create(42)).size();
			for (Direction d : Direction.values())
				quads += model.getQuads(null, d, net.minecraft.util.RandomSource.create(42)).size();
			if (model == missing || (quads == 0 && !model.isCustomRenderer())) {
				invisible++;
				Create.LOGGER.warn("[harness] item model empty: {} ({}{})", net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item), model.getClass().getSimpleName(), model == missing ? ", MISSING" : "");
			}
		}
		Create.LOGGER.info("[harness] item models probed: {} total, {} empty", total, invisible);
	}

	/** Logs whether Catnip's SuperByteBuffer path (Ponder / contraption rendering) produces geometry for a few models. */
	private static void probeBuffers(Minecraft mc) {
		BlockState[] states = { AllBlocks.COGWHEEL.getDefaultState(), AllBlocks.SHAFT.getDefaultState(), AllBlocks.LARGE_COGWHEEL.getDefaultState(),
			AllBlocks.CREATIVE_MOTOR.getDefaultState(), Blocks.STONE.defaultBlockState(), AllBlocks.ANDESITE_CASING.getDefaultState() };
		for (BlockState state : states) {
			var model = mc.getBlockRenderer().getBlockModel(state);
			boolean empty = net.createmod.catnip.render.SuperBufferFactory.getInstance().createForBlock(state).isEmpty();
			int quads = 0;
			for (Direction d : Direction.values()) quads += model.getQuads(state, d, net.minecraft.util.RandomSource.create(42)).size();
			quads += model.getQuads(state, null, net.minecraft.util.RandomSource.create(42)).size();
			Create.LOGGER.info("[harness] sbb {} model={} vanillaAdapter={} quads={} sbbEmpty={}", state.getBlock().getName().getString(), model.getClass().getSimpleName(),
				((net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel) model).isVanillaAdapter(), quads, empty);
		}
	}

	/** {@code CREATE_HARNESS_PONDER=<item id>} picks the Ponder scene (default cogwheel). */
	private static ItemStack ponderTarget() {
		String id = System.getProperty("create.harness.ponder", System.getenv("CREATE_HARNESS_PONDER"));
		if (id == null)
			return AllBlocks.COGWHEEL.asStack();
		return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(id)));
	}

	private static void openCreativeTab(Minecraft mc, net.minecraft.world.item.CreativeModeTab tab) {
		net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen screen =
			new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true);
		mc.setScreen(screen);
		net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen fabric = (net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen) screen;
		fabric.switchToPage(fabric.getPage(tab));
		fabric.setSelectedItemGroup(tab);
	}

	private static void screenshot(Minecraft mc, String name) {
		try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
			File file = new File(mc.gameDirectory, "screenshots/create-harness-" + name + ".png");
			file.getParentFile().mkdirs();
			image.writeToFile(file);
			Create.LOGGER.info("[harness] wrote {}", file.getAbsolutePath());
		} catch (Exception e) {
			Create.LOGGER.error("[harness] screenshot failed", e);
		}
	}

	private static void teleport(Minecraft mc, double x, double y, double z, float yaw, float pitch, boolean waitForChunks) {
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
			if (player != null) {
				player.getAbilities().flying = true;
				player.onUpdateAbilities();
				player.teleportTo(server.overworld(), x, y, z, yaw, pitch);
			}
		});
	}

	private static void build(Minecraft mc) {
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> {
			ServerLevel level = server.overworld();
			ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
			player.setGameMode(GameType.CREATIVE);
			player.getAbilities().mayfly = true;
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
			level.setDayTime(6000);
			level.setWeatherParameters(6000, 0, false, false);
			// floor
			for (int x = -2; x <= 25; x++)
				for (int z = -2; z <= 12; z++)
					level.setBlockAndUpdate(BASE.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
			Consumer<Object[]> place = args -> {
				BlockPos pos = BASE.offset((Integer) args[0], (Integer) args[1], (Integer) args[2]);
				level.setBlockAndUpdate(pos, (BlockState) args[3]);
			};
			// row A (z=1): kinetics
			place.accept(new Object[] { 1, 0, 1, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST) });
			place.accept(new Object[] { 2, 0, 1, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X) });
			place.accept(new Object[] { 3, 0, 1, AllBlocks.COGWHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X) });
			place.accept(new Object[] { 4, 0, 1, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X) });
			place.accept(new Object[] { 5, 0, 1, AllBlocks.STRESSOMETER.getDefaultState() });
			place.accept(new Object[] { 6, 0, 1, AllBlocks.SPEEDOMETER.getDefaultState() });
			place.accept(new Object[] { 7, 0, 1, AllBlocks.ENCASED_FAN.getDefaultState() });
			place.accept(new Object[] { 8, 0, 1, AllBlocks.HAND_CRANK.getDefaultState() });
			place.accept(new Object[] { 9, 0, 1, AllBlocks.MILLSTONE.getDefaultState() });
			place.accept(new Object[] { 10, 0, 1, AllBlocks.BASIN.getDefaultState() });
			place.accept(new Object[] { 10, 2, 1, AllBlocks.MECHANICAL_MIXER.getDefaultState() });
			place.accept(new Object[] { 11, 0, 1, AllBlocks.MECHANICAL_PRESS.getDefaultState() });
			place.accept(new Object[] { 12, 0, 1, AllBlocks.CRUSHING_WHEEL.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X) });
			place.accept(new Object[] { 14, 0, 1, AllBlocks.DEPLOYER.getDefaultState() });
			place.accept(new Object[] { 15, 0, 1, AllBlocks.MECHANICAL_ARM.getDefaultState() });
			place.accept(new Object[] { 16, 0, 1, AllBlocks.MECHANICAL_CRAFTER.getDefaultState() });
			place.accept(new Object[] { 17, 0, 1, AllBlocks.STEAM_ENGINE.getDefaultState() });
			place.accept(new Object[] { 19, 0, 1, AllBlocks.WATER_WHEEL.getDefaultState() });
			place.accept(new Object[] { 22, 1, 1, AllBlocks.LARGE_WATER_WHEEL.getDefaultState() });
			// row B (z=5): obj / dynamic models, fluids, copycats, CT
			place.accept(new Object[] { 1, 0, 5, AllBlocks.FLYWHEEL.getDefaultState() });
			place.accept(new Object[] { 3, 0, 5, AllBlocks.CHAIN_CONVEYOR.getDefaultState() });
			place.accept(new Object[] { 5, 0, 5, AllBlocks.COPPER_VALVE_HANDLE.getDefaultState() });
			place.accept(new Object[] { 7, 0, 5, AllBlocks.BLAZE_BURNER.getDefaultState().setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.KINDLED) });
			place.accept(new Object[] { 9, 0, 5, AllBlocks.FLUID_TANK.getDefaultState() });
			place.accept(new Object[] { 11, 0, 5, AllBlocks.FLUID_PIPE.getDefaultState() });
			place.accept(new Object[] { 12, 0, 5, AllBlocks.SPOUT.getDefaultState() });
			place.accept(new Object[] { 14, 0, 5, AllBlocks.COPYCAT_PANEL.getDefaultState() });
			place.accept(new Object[] { 15, 0, 5, AllBlocks.COPYCAT_STEP.getDefaultState() });
			for (int x = 17; x <= 19; x++)
				for (int y = 0; y <= 1; y++)
					place.accept(new Object[] { x, y, 5, AllPaletteBlocks.FRAMED_GLASS.getDefaultState() });
			for (int x = 21; x <= 22; x++)
				for (int y = 0; y <= 1; y++)
					place.accept(new Object[] { x, y, 5, AllBlocks.BRASS_CASING.getDefaultState() });
			// row C (z=9): logistics / trains / redstone
			for (int x = 1; x <= 5; x++)
				place.accept(new Object[] { x, 0, 9, AllBlocks.TRACK.getDefaultState().setValue(TrackBlock.SHAPE, TrackShape.XO) });
			place.accept(new Object[] { 7, 0, 9, AllBlocks.ORANGE_NIXIE_TUBE.getDefaultState() });
			place.accept(new Object[] { 8, 0, 9, AllBlocks.DISPLAY_BOARD.getDefaultState() });
			place.accept(new Object[] { 10, 0, 9, AllBlocks.ITEM_VAULT.getDefaultState() });
			place.accept(new Object[] { 12, 0, 9, AllBlocks.CHUTE.getDefaultState() });
			place.accept(new Object[] { 13, 0, 9, AllBlocks.ANDESITE_FUNNEL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP) });
			place.accept(new Object[] { 15, 0, 9, AllBlocks.DEPOT.getDefaultState() });
			place.accept(new Object[] { 16, 0, 9, AllBlocks.PACKAGER.getDefaultState() });
			place.accept(new Object[] { 17, 0, 9, AllBlocks.STOCK_TICKER.getDefaultState() });
			place.accept(new Object[] { 19, 0, 9, AllBlocks.ROSE_QUARTZ_LAMP.getDefaultState() });
			place.accept(new Object[] { 20, 0, 9, AllBlocks.REDSTONE_LINK.getDefaultState() });
			// spinning bearing contraption (motor under the floor → bearing → planks + cog + glass + casing)
			place.accept(new Object[] { 22, -1, 9, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP) });
			place.accept(new Object[] { 22, 0, 9, AllBlocks.MECHANICAL_BEARING.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP) });
			place.accept(new Object[] { 22, 1, 9, Blocks.OAK_PLANKS.defaultBlockState() });
			place.accept(new Object[] { 23, 1, 9, AllBlocks.COGWHEEL.getDefaultState() });
			place.accept(new Object[] { 21, 1, 9, AllPaletteBlocks.FRAMED_GLASS.getDefaultState() });
			place.accept(new Object[] { 22, 1, 10, AllBlocks.ANDESITE_CASING.getDefaultState() });
			place.accept(new Object[] { 22, 2, 9, AllBlocks.SHAFT.getDefaultState() });
			// block entity data
			if (level.getBlockEntity(BASE.offset(14, 0, 5)) instanceof CopycatBlockEntity copycat)
				copycat.setMaterial(Blocks.SPRUCE_PLANKS.defaultBlockState());
			if (level.getBlockEntity(BASE.offset(15, 0, 5)) instanceof CopycatBlockEntity copycat)
				copycat.setMaterial(Blocks.STONE_BRICKS.defaultBlockState());
			IFluidHandler tank = Capabilities.FluidHandler.BLOCK.find(level, BASE.offset(9, 0, 5), null);
			if (tank != null)
				tank.fill(new FluidStack(Fluids.WATER, 6000), IFluidHandler.FluidAction.EXECUTE);
			if (level.getBlockEntity(BASE.offset(15, 0, 9)) instanceof DepotBlockEntity depot)
				depot.setHeldItem(PackageStyles.getDefaultBox());
			// player: creative, hotbar of custom-rendered items, goggles on
			player.getInventory().clearContent();
			ItemStack[] hotbar = { AllItems.WRENCH.asStack(), AllItems.GOGGLES.asStack(), AllItems.WORLDSHAPER.asStack(), AllItems.POTATO_CANNON.asStack(),
				AllItems.EXTENDO_GRIP.asStack(), AllItems.CRAFTING_BLUEPRINT.asStack(), PackageStyles.getDefaultBox(), AllItems.LINKED_CONTROLLER.asStack(),
				AllItems.SCHEMATIC_AND_QUILL.asStack() };
			for (int i = 0; i < hotbar.length; i++)
				player.getInventory().setItem(i, hotbar[i]);
			player.setItemSlot(EquipmentSlot.HEAD, AllItems.GOGGLES.asStack());
			player.getInventory().selected = 0;
			player.teleportTo(level, 12.5, 153.0, -8.5, 0, 25);
			Create.LOGGER.info("[harness] showcase built");
		});
	}
}
