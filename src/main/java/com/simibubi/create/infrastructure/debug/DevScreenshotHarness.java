package com.simibubi.create.infrastructure.debug;

import java.io.File;
import java.util.List;
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
	private static boolean openedMap = false;
	private static boolean done;

	public static void init() {
		String mode = System.getProperty("create.harness", System.getenv("CREATE_HARNESS"));
		if ("items".equals(mode)) {
			Create.LOGGER.info("[harness] held-item harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(ItemHarness::tick);
			return;
		}
		if ("gui".equals(mode)) {
			Create.LOGGER.info("[harness] GUI item harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(GuiHarness::tick);
			return;
		}
		if ("schematic".equals(mode)) {
			Create.LOGGER.info("[harness] schematic tool menu harness armed");
			// stay off the schematic slot until setup has put a valid schematic there
			ClientTickEvents.START_CLIENT_TICK.register(mc -> { if (mc.player != null && phase == 0 && tick < 30) mc.player.getInventory().selected = 8; });
			ClientTickEvents.END_CLIENT_TICK.register(SchematicKeyHarness::tick);
			return;
		}
		if ("transfer".equals(mode)) {
			Create.LOGGER.info("[harness] transfer benchmark armed");
			ClientTickEvents.END_CLIENT_TICK.register(TransferBenchmark::tick);
			return;
		}
		if ("drawers".equals(mode)) {
			Create.LOGGER.info("[harness] drawer network benchmark armed");
			ClientTickEvents.END_CLIENT_TICK.register(DrawerNetworkBenchmark::tick);
			return;
		}
		if ("crumble".equals(mode)) {
			Create.LOGGER.info("[harness] drawer crumbling harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(CrumbleHarness::tick);
			return;
		}
		if ("machines".equals(mode)) {
			Create.LOGGER.info("[harness] machine item harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(MachineItemsHarness::tick);
			return;
		}
		if ("opac".equals(mode)) {
			Create.LOGGER.info("[harness] OPAC claim protection harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(OpacHarness::tick);
			return;
		}
		if ("gantry".equals(mode)) {
			Create.LOGGER.info("[harness] moving gantry render harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(GantryRenderHarness::tick);
			return;
		}
		if ("usekey".equals(mode)) {
			Create.LOGGER.info("[harness] use key repeat harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(UseKeyHarness::tick);
			return;
		}
		if ("emipotion".equals(mode)) {
			Create.LOGGER.info("[harness] EMI potion lookup harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(EmiPotionHarness::tick);
			return;
		}
		if ("keys".equals(mode)) {
			Create.LOGGER.info("[harness] key mapping harness armed");
			ClientTickEvents.END_CLIENT_TICK.register(KeyHarness::tick);
			return;
		}
		if (!"showcase".equals(mode))
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
				case 0 -> { if (tick >= 40) { build(mc); probeBuffers(mc); probeItemModels(mc); probeClientRecipes(mc); phase = 1; tick = 0; } }
				case 1 -> { if (tick == 5) mc.player.getInventory().selected = 0; if (tick >= 140) { screenshot(mc, "overview"); teleport(mc, 5.5, 152.0, -1.5, 0, 46, false); phase = 2; tick = 0; } }
				case 2 -> { if (tick >= 60) { screenshot(mc, "goggles"); teleport(mc, 22.5, 154.5, 3.5, 0, 35, false); phase = 5; tick = 0; } }
				case 5 -> { if (tick >= 60) { screenshot(mc, "contraption"); teleport(mc, 0.5, 152.0, -3.8, 0, 30, false); phase = 20; tick = 0; } }
				case 20 -> { if (tick >= 60) { logContraptions(mc); screenshot(mc, "contraption-ct"); openCreativeTab(mc, com.simibubi.create.AllCreativeModeTabs.BASE_CREATIVE_TAB.get()); phase = 6; tick = 0; } }
				case 6 -> { if (tick >= 30) { screenshot(mc, "creative-base"); openCreativeTab(mc, com.simibubi.create.AllCreativeModeTabs.PALETTES_CREATIVE_TAB.get()); phase = 7; tick = 0; } }
				case 7 -> { if (tick >= 30) { screenshot(mc, "creative-palettes"); mc.setScreen(new net.createmod.catnip.config.ui.BaseConfigScreen(null, Create.ID)); phase = 8; tick = 0; } }
				case 8 -> { if (tick >= 30) { screenshot(mc, "config"); mc.setScreen(null); if (com.simibubi.create.compat.Mods.EMI.isLoaded() && EmiHarness.show(mc, 0)) phase = 13; else phase = 16; tick = 0; } }
				case 16 -> { if (com.simibubi.create.compat.Mods.JEI.isLoaded() && JeiHarness.showRecipes(mc)) { phase = 9; } else { mc.setScreen(null); openMenuAt(mc, BASE.offset(24, 0, 12)); phase = 11; } tick = 0; }
				case 9 -> { if (tick >= 40) { screenshot(mc, "jei-mixing"); JeiHarness.next(mc); phase = 10; tick = 0; } }
				case 10 -> { if (tick >= 40) { screenshot(mc, "jei-next"); mc.setScreen(null); openMenuAt(mc, BASE.offset(24, 0, 12)); phase = 11; tick = 0; } }
				case 11 -> { if (tick >= 40) { screenshot(mc, "menu-schematic-table"); mc.setScreen(null); openMenuAt(mc, BASE.offset(24, 0, 11)); phase = 12; tick = 0; } }
				case 12 -> { if (tick >= 40) { screenshot(mc, "menu-schematicannon"); mc.setScreen(null); openedMap = com.simibubi.create.compat.Mods.XAEROWORLDMAP.isLoaded() && MapHarness.openXaero(mc); phase = 17; tick = 0; } }
				case 13 -> { if (tick >= 40) { screenshot(mc, "emi-1"); EmiHarness.show(mc, 1); phase = 14; tick = 0; } }
				case 14 -> { if (tick >= 40) { screenshot(mc, "emi-2"); EmiHarness.show(mc, 2); phase = 15; tick = 0; } }
				case 15 -> { if (tick >= 40) { screenshot(mc, "emi-3"); mc.setScreen(null); phase = 16; tick = 0; } }
				// train map overlays (compat/trainmap) — each only if that map mod is installed
				case 17 -> { if (tick >= 40) { if (openedMap) screenshot(mc, "trainmap-xaero"); mc.setScreen(null); openedMap = com.simibubi.create.compat.Mods.JOURNEYMAP.isLoaded() && MapHarness.openJourneyMap(mc); phase = 18; tick = 0; } }
				case 18 -> { if (tick >= 40) { if (openedMap) screenshot(mc, "trainmap-journeymap"); mc.setScreen(null); openedMap = com.simibubi.create.compat.Mods.FTBCHUNKS.isLoaded() && MapHarness.openFtbChunks(mc); phase = 19; tick = 0; } }
				case 19 -> { if (tick >= 40) { if (openedMap) screenshot(mc, "trainmap-ftbchunks"); mc.setScreen(null); mc.setScreen(PonderUI.of(ponderTarget())); phase = 3; tick = 0; } }
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

	private static void logContraptions(Minecraft mc) {
		Create.LOGGER.info("[harness] block above CT bearing: {}", mc.level.getBlockState(BASE.offset(0, 1, -1)));
		for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering())
			if (e instanceof com.simibubi.create.content.contraptions.AbstractContraptionEntity ace)
				Create.LOGGER.info("[harness] contraption entity {} at {} blocks={}", e.getType(), e.blockPosition(),
					ace.getContraption() == null ? null : ace.getContraption().getBlocks().values().stream().map(i -> i.state().getBlock().toString()).toList());
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

	private static void probeClientRecipes(Minecraft mc) {
		for (String id : new String[] { "create:crafting/logistics/package_frogport", "create:crafting/kinetics/sticky_mechanical_piston", "minecraft:stick" }) {
			var holder = mc.getConnection().getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.parse(id)).orElse(null);
			if (holder == null || !(holder.value() instanceof net.minecraft.world.item.crafting.ShapedRecipe recipe)) {
				Create.LOGGER.info("[harness] client recipe {} = {}", id, holder == null ? null : holder.value().getClass().getSimpleName());
				continue;
			}
			Create.LOGGER.info("[harness] client recipe {} w={} h={} ingredients={} class={}", id, recipe.getWidth(), recipe.getHeight(), recipe.getIngredients().size(), recipe.getClass().getName());
		}
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

	/**
	 * {@code -Dcreate.harness=items}: first-person (and third-person) screenshots of the custom-rendered handheld items
	 * (potato cannon, extendo grip, wand of symmetry, wrench for reference), plus a log of their partial models.
	 */
	private static final class ItemHarness {
		private static final String[] NAMES = { "wrench", "potato_cannon", "extendo_grip", "wand_of_symmetry" };

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			try {
				if (phase == 0) {
					if (tick < 40)
						return;
					setup(mc);
					logPartials(mc);
					phase = 1;
					tick = 0;
					return;
				}
				int slot = (phase - 1) / 2;
				boolean thirdPerson = (phase - 1) % 2 == 1;
				if (slot >= NAMES.length) {
					Create.LOGGER.info("[harness] done");
					done = true;
					mc.stop();
					return;
				}
				if (tick == 1) {
					mc.player.getInventory().selected = slot;
					mc.options.setCameraType(thirdPerson ? net.minecraft.client.CameraType.THIRD_PERSON_FRONT : net.minecraft.client.CameraType.FIRST_PERSON);
				}
				if (tick >= 40) {
					screenshot(mc, "item-" + NAMES[slot] + (thirdPerson ? "-3p" : "-1p"));
					phase++;
					tick = 0;
				}
			} catch (Throwable t) {
				Create.LOGGER.error("[harness] failed", t);
				done = true;
				mc.stop();
			}
		}

		private static void setup(Minecraft mc) {
			MinecraftServer server = mc.getSingleplayerServer();
			server.execute(() -> {
				ServerLevel level = server.overworld();
				ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
				player.setGameMode(GameType.CREATIVE);
				player.getAbilities().flying = true;
				player.onUpdateAbilities();
				level.setDayTime(6000);
				for (int x = -3; x <= 3; x++)
					for (int z = -3; z <= 3; z++)
						level.setBlockAndUpdate(BASE.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
				player.getInventory().clearContent();
				ItemStack[] hotbar = { AllItems.WRENCH.asStack(), AllItems.POTATO_CANNON.asStack(), AllItems.EXTENDO_GRIP.asStack(),
					AllItems.WAND_OF_SYMMETRY.asStack() };
				for (int i = 0; i < hotbar.length; i++)
					player.getInventory().setItem(i, hotbar[i]);
				player.teleportTo(level, 0.5, 151.5, 0.5, 0, 10);
			});
		}

		private static void logPartials(Minecraft mc) {
			var missing = mc.getModelManager().getMissingModel();
			for (String path : new String[] { "item/potato_cannon/cog", "item/extendo_grip/cog", "item/extendo_grip/thin_short",
				"item/extendo_grip/wide_long", "item/wand_of_symmetry/bits", "item/wand_of_symmetry/core" }) {
				var partial = dev.engine_room.flywheel.lib.model.baked.PartialModel.of(Create.asResource(path));
				var model = partial.get();
				int quads = 0;
				if (model != null) {
					quads += model.getQuads(null, null, net.minecraft.util.RandomSource.create(42)).size();
					for (Direction d : Direction.values())
						quads += model.getQuads(null, d, net.minecraft.util.RandomSource.create(42)).size();
				}
				Create.LOGGER.info("[harness] partial {} -> {} quads={}{}", path, model == null ? null : model.getClass().getSimpleName(), quads,
					model == missing ? " MISSING" : "");
			}
			for (ItemStack stack : new ItemStack[] { AllItems.WRENCH.asStack(), AllItems.POTATO_CANNON.asStack(), AllItems.EXTENDO_GRIP.asStack(),
				AllItems.WAND_OF_SYMMETRY.asStack() }) {
				var model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
				Create.LOGGER.info("[harness] item {} -> {} custom={} bewlr={}", stack.getItem(), model.getClass().getName(), model.isCustomRenderer(),
					com.simibubi.create.foundation.client.extensions.IClientItemExtensions.of(stack).getCustomRenderer().getClass().getName());
			}
		}
	}

	/**
	 * {@code -Dcreate.harness=gui}: Create's machine items in GUIs: the creative Create tab and a few EMI recipe pages
	 * (if EMI is present). Screenshot names carry {@code CREATE_HARNESS_TAG} so runs under different setups don't collide.
	 */
	private static final class GuiHarness {
		private static final String[] EMI_PAGES = { "mixing", "pressing", "deploying", "sequenced_assembly" };
		private static final String TAG = System.getenv().getOrDefault("CREATE_HARNESS_TAG", "default");

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			try {
				if (phase == 0) {
					if (tick == 20) {
						MinecraftServer server = mc.getSingleplayerServer();
						server.execute(() -> server.getPlayerList().getPlayer(mc.player.getUUID()).setGameMode(GameType.CREATIVE));
					}
					if (tick < 40)
						return;
					Create.LOGGER.info("[harness] gui {}: flywheel backend={} supportsVisualization={}", TAG,
						dev.engine_room.flywheel.api.backend.BackendManager.currentBackend(),
						dev.engine_room.flywheel.api.visualization.VisualizationManager.supportsVisualization(mc.level));
					probeVertices(com.simibubi.create.AllBlocks.MECHANICAL_PRESS.getDefaultState(), null);
					probeVertices(com.simibubi.create.AllBlocks.BASIN.getDefaultState(), null);
					probeVertices(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), null);
					probeVertices(null, com.simibubi.create.AllPartialModels.MECHANICAL_PRESS_HEAD);
					openCreativeTab(mc, com.simibubi.create.AllCreativeModeTabs.BASE_CREATIVE_TAB.get());
					phase = 1;
					tick = 0;
					return;
				}
				if (phase == 1) {
					if (tick >= 30) {
						screenshot(mc, "gui-" + TAG + "-creative");
						phase = 2;
						tick = 0;
					}
					return;
				}
				int page = phase - 2;
				boolean emi = com.simibubi.create.compat.Mods.EMI.isLoaded();
				if (!emi || page >= EMI_PAGES.length) {
					mc.setScreen(null);
					Create.LOGGER.info("[harness] done");
					done = true;
					mc.stop();
					return;
				}
				if (tick == 1 && !EmiPage.show(EMI_PAGES[page])) {
					phase++;
					tick = 0;
					return;
				}
				if (tick >= 30) {
					screenshot(mc, "gui-" + TAG + "-emi-" + EMI_PAGES[page]);
					phase++;
					tick = 0;
				}
			} catch (Throwable t) {
				Create.LOGGER.error("[harness] failed", t);
				done = true;
				mc.stop();
			}
		}
		/** Buffers a block model the way Catnip's GuiGameElement does and logs the first vertices' color/light/normal. */
		private static void probeVertices(@Nullable BlockState state, @Nullable dev.engine_room.flywheel.lib.model.baked.PartialModel partial) {
			Minecraft mc = Minecraft.getInstance();
			BlockState blockState = state != null ? state : Blocks.AIR.defaultBlockState();
			var model = partial != null ? partial.get() : mc.getBlockRenderer().getBlockModel(blockState);
			String name = partial != null ? partial.modelLocation().toString() : blockState.toString();
			int[] count = { 0 };
			StringBuilder out = new StringBuilder();
			net.createmod.catnip.client.render.model.ShadeSeparatedBufferSource source = (type, shade) -> new com.mojang.blaze3d.vertex.VertexConsumer() {
				public com.mojang.blaze3d.vertex.VertexConsumer addVertex(float x, float y, float z) {
					if (count[0]++ < 8) out.append(String.format("%n  [%s shade=%s] pos=(%.2f,%.2f,%.2f)", type, shade, x, y, z));
					return this;
				}
				public com.mojang.blaze3d.vertex.VertexConsumer setColor(int r, int g, int b, int a) {
					if (count[0] <= 8) out.append(String.format(" color=%d,%d,%d,%d", r, g, b, a));
					return this;
				}
				public com.mojang.blaze3d.vertex.VertexConsumer setUv(float u, float v) { return this; }
				public com.mojang.blaze3d.vertex.VertexConsumer setUv1(int u, int v) { return this; }
				public com.mojang.blaze3d.vertex.VertexConsumer setUv2(int u, int v) {
					if (count[0] <= 8) out.append(String.format(" light=%d,%d", u, v));
					return this;
				}
				public com.mojang.blaze3d.vertex.VertexConsumer setNormal(float x, float y, float z) {
					if (count[0] <= 8) out.append(String.format(" normal=%.2f,%.2f,%.2f", x, y, z));
					return this;
				}
			};
			var getter = dev.engine_room.flywheel.lib.model.baked.SinglePosVirtualBlockGetter.createFullBright().blockState(blockState);
			net.createmod.catnip.client.render.model.BakedModelBufferer.bufferModel(model, BlockPos.ZERO, getter, blockState,
				new com.mojang.blaze3d.vertex.PoseStack(), source);
			Create.LOGGER.info("[harness] gui {} vertices {} model={} vanillaAdapter={} total={}{}", TAG, name, model.getClass().getSimpleName(),
				((net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel) model).isVanillaAdapter(), count[0], out);
		}
	}

	/** Separate class so EMI classes only load when EMI is present. */
	private static final class EmiPage {
		static boolean show(String path) {
			for (var category : dev.emi.emi.api.EmiApi.getRecipeManager().getCategories())
				if (category.getId().getNamespace().equals(Create.ID) && category.getId().getPath().equals(path)) {
					dev.emi.emi.api.EmiApi.displayRecipeCategory(category);
					return true;
				}
			Create.LOGGER.info("[harness] no EMI category create:{}", path);
			return false;
		}
	}

	/**
	 * {@code -Dcreate.harness=schematic}: holds a schematic and presses/releases the Tool Menu key through vanilla's key
	 * callback (bound to Left Alt, then Right Alt, then '.'), logging whether the tool selection focuses and unfocuses.
	 */
	private static final class SchematicKeyHarness {
		private static final int[] KEYS = { org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT, org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT,
			org.lwjgl.glfw.GLFW.GLFW_KEY_PERIOD };
		private static com.mojang.blaze3d.platform.InputConstants.Key originalKey;
		private static int failures;

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			try {
				var toolMenu = com.simibubi.create.AllKeys.TOOL_MENU.getKeybind();
				if (phase == 0) {
					if (tick == 20)
						setup(mc);
					if (tick == 30)
						mc.player.getInventory().selected = 0;
					if (tick >= 60) {
						originalKey = toolMenu.getKey();
						Create.LOGGER.info("[harness] schematic active={}", field("active"));
						phase = 1;
						tick = 0;
					}
					return;
				}
				int index = (phase - 1) / 3, step = (phase - 1) % 3;
				if (index >= KEYS.length) {
					toolMenu.setKey(originalKey);
					net.minecraft.client.KeyMapping.resetMapping();
					Create.LOGGER.info("[harness] schematic done, failures={}", failures);
					Create.LOGGER.info("[harness] done");
					done = true;
					mc.stop();
					return;
				}
				int glfw = KEYS[index];
				if (tick < 5)
					return;
				switch (step) {
					case 0 -> {
						toolMenu.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(glfw));
						net.minecraft.client.KeyMapping.resetMapping();
						press(mc, glfw, org.lwjgl.glfw.GLFW.GLFW_PRESS);
						press(mc, glfw, org.lwjgl.glfw.GLFW.GLFW_REPEAT);
					}
					case 1 -> {
						check(toolMenu.getKey().getName() + " press -> focused", focused());
						press(mc, glfw, org.lwjgl.glfw.GLFW.GLFW_RELEASE);
					}
					case 2 -> check(toolMenu.getKey().getName() + " release -> unfocused", !focused());
				}
				phase++;
				tick = 0;
			} catch (Throwable t) {
				Create.LOGGER.error("[harness] failed", t);
				done = true;
				mc.stop();
			}
		}

		private static void setup(Minecraft mc) {
			MinecraftServer server = mc.getSingleplayerServer();
			server.execute(() -> {
				ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
				player.setGameMode(GameType.CREATIVE);
				ItemStack blueprint = AllItems.SCHEMATIC.asStack();
				blueprint.set(com.simibubi.create.AllDataComponents.SCHEMATIC_DEPLOYED, false);
				blueprint.set(com.simibubi.create.AllDataComponents.SCHEMATIC_OWNER, player.getGameProfile().getName());
				blueprint.set(com.simibubi.create.AllDataComponents.SCHEMATIC_FILE, "harness.nbt");
				blueprint.set(com.simibubi.create.AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ZERO);
				blueprint.set(com.simibubi.create.AllDataComponents.SCHEMATIC_ROTATION, net.minecraft.world.level.block.Rotation.NONE);
				blueprint.set(com.simibubi.create.AllDataComponents.SCHEMATIC_MIRROR, net.minecraft.world.level.block.Mirror.NONE);
				blueprint.set(com.simibubi.create.AllDataComponents.SCHEMATIC_BOUNDS, new net.minecraft.core.Vec3i(3, 3, 3));
				player.getInventory().setItem(0, blueprint);
			});
		}

		private static void press(Minecraft mc, int glfwKey, int action) {
			mc.keyboardHandler.keyPress(mc.getWindow().getWindow(), glfwKey, 0, action, 0);
		}

		private static boolean focused() throws ReflectiveOperationException {
			Object screen = field("selectionScreen");
			return ((com.simibubi.create.content.schematics.client.ToolSelectionScreen) screen).focused;
		}

		private static Object field(String name) throws ReflectiveOperationException {
			var f = com.simibubi.create.content.schematics.client.SchematicHandler.class.getDeclaredField(name);
			f.setAccessible(true);
			return f.get(com.simibubi.create.CreateClient.SCHEMATIC_HANDLER);
		}

		private static void check(String what, boolean ok) {
			if (!ok)
				failures++;
			Create.LOGGER.info("[harness] schematic {} {}", ok ? "PASS" : "FAIL", what);
		}
	}

	/**
	 * {@code -Dcreate.harness=transfer}: times Create's item handler calls against a big, full Fabric storage (like a
	 * Tom's Storage network under a chute): a {@code CombinedStorage} of 50 double-chest-sized inventories (2700 slots)
	 * full of stone, and the same as a slotted storage. Runs on the server thread.
	 */
	private static final class TransferBenchmark {
		private static final BlockPos CHUTES = BASE.offset(40, 0, 40);
		private static final int CHUTE_COUNT = 20;
		private static net.fabricmc.fabric.api.transfer.v1.storage.Storage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant> network;

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			tick++;
			MinecraftServer server = mc.getSingleplayerServer();
			// micro benchmark, then 20 real chutes holding a diamond above the full network: average tick time with/without
			switch (phase) {
				case 0 -> { if (tick >= 40) { server.execute(() -> { try { run(); placeChutes(server, true); } catch (Throwable t) { Create.LOGGER.error("[harness] transfer failed", t); } }); phase = 1; tick = 0; } }
				case 1 -> { if (tick >= 200) { server.execute(() -> Create.LOGGER.info("[harness] transfer {} chutes over the full network: avg tick {} ms", CHUTE_COUNT, String.format("%.2f", server.getAverageTickTimeNanos() / 1e6))); server.execute(() -> placeChutes(server, false)); phase = 2; tick = 0; } }
				case 2 -> { if (tick >= 200) { server.execute(() -> Create.LOGGER.info("[harness] transfer no chutes: avg tick {} ms", String.format("%.2f", server.getAverageTickTimeNanos() / 1e6))); server.execute(() -> { placeChutes(server, true); for (int i = 0; i < CHUTE_COUNT; i++) server.overworld().setBlockAndUpdate(CHUTES.offset(i, 0, 0), Blocks.STONE.defaultBlockState()); }); phase = 4; tick = 0; } }
				case 4 -> { if (tick >= 200) { server.execute(() -> Create.LOGGER.info("[harness] transfer {} chutes over plain stone: avg tick {} ms", CHUTE_COUNT, String.format("%.2f", server.getAverageTickTimeNanos() / 1e6))); server.execute(() -> placeChutes(server, false)); phase = 3; tick = 0; } }
				case 3 -> { if (tick >= 5) { Create.LOGGER.info("[harness] done"); done = true; mc.stop(); } }
				default -> {}
			}
		}

		private static void placeChutes(MinecraftServer server, boolean place) {
			ServerLevel level = server.overworld();
			if (place && network == null) {
				var stored = network = fullNetwork();
				net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerFallback((world, pos, state, be, side) ->
					pos.getY() == CHUTES.getY() && pos.getX() >= CHUTES.getX() && pos.getX() < CHUTES.getX() + CHUTE_COUNT
						&& pos.getZ() == CHUTES.getZ() && state.is(Blocks.BEDROCK) ? stored : null);
			}
			for (int i = 0; i < CHUTE_COUNT; i++) {
				BlockPos below = CHUTES.offset(i, 0, 0), chute = below.above();
				level.setBlockAndUpdate(below, place ? Blocks.BEDROCK.defaultBlockState() : Blocks.AIR.defaultBlockState());
				level.setBlockAndUpdate(chute, place ? AllBlocks.CHUTE.getDefaultState() : Blocks.AIR.defaultBlockState());
				if (place && level.getBlockEntity(chute) instanceof com.simibubi.create.content.logistics.chute.ChuteBlockEntity be)
					be.setItem(new ItemStack(net.minecraft.world.item.Items.DIAMOND));
			}
		}

		private static net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant,
			net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant>> fullNetwork() {
			List<net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant>> parts = new java.util.ArrayList<>();
			for (int i = 0; i < 50; i++) {
				net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(54);
				for (int slot = 0; slot < 54; slot++)
					container.setItem(slot, new ItemStack(net.minecraft.world.item.Items.STONE, 64));
				parts.add(net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage.of(container, null));
			}
			return new net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage<>(parts);
		}

		private static void run() {
			var combined = fullNetwork();
			var parts = combined.parts;
			var slotted = new net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage<>(parts);
			for (var entry : java.util.Map.of("combined", combined, "slotted", slotted).entrySet()) {
				var storage = entry.getValue();
				ItemStack diamond = new ItemStack(net.minecraft.world.item.Items.DIAMOND);
				com.simibubi.create.foundation.transfer.item.IItemHandler handler =
					com.simibubi.create.foundation.transfer.capability.bridge.StorageItemHandler.of(storage);
				time(entry.getKey() + " insertItemStacked (full, chute output)", () ->
					com.simibubi.create.foundation.transfer.item.ItemHandlerHelper.insertItemStacked(handler, diamond, true));
				time(entry.getKey() + " extract diamond (absent, funnel/chute input)", () ->
					com.simibubi.create.foundation.item.ItemHelper.extract(handler, s -> s.is(net.minecraft.world.item.Items.DIAMOND), 64, true));
				time(entry.getKey() + " fresh handler + insertItemStacked", () ->
					com.simibubi.create.foundation.transfer.item.ItemHandlerHelper.insertItemStacked(
						com.simibubi.create.foundation.transfer.capability.bridge.StorageItemHandler.of(storage), diamond, true));
			}
		}

		static void time(String what, Runnable op) {
			op.run();
			int runs = 0;
			long start = System.nanoTime(), elapsed;
			do {
				op.run();
				runs++;
				elapsed = System.nanoTime() - start;
			} while (elapsed < 1_000_000_000L && runs < 20_000);
			Create.LOGGER.info("[harness] transfer {}: {} us/call ({} runs)", what, String.format("%.1f", elapsed / 1000.0 / runs), runs);
		}
	}

	/**
	 * {@code -Dcreate.harness=drawers} (needs Extended Drawers in run/mods): a chute holding a diamond over an Extended
	 * Drawers access point whose network is a 35x20 wall of quad drawers (2800 slots), each slot holding stone, so the
	 * diamond fits nowhere. Logs the insert call and the chute's own tick time (timed around an extra tick() per server
	 * tick), as Observable showed on the live server.
	 */
	private static final class DrawerNetworkBenchmark {
		private static final BlockPos ACCESS = BASE.offset(-60, 0, 40);
		private static final int WIDTH = 35, HEIGHT = 20;
		private static long chuteNanos;
		private static int chuteTicks;
		private static boolean timing;

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			tick++;
			MinecraftServer server = mc.getSingleplayerServer();
			switch (phase) {
				case 0 -> { if (tick >= 40) { server.execute(() -> build(server)); phase = 1; tick = 0; } }
				case 1 -> { if (tick >= 60) { server.execute(() -> fill(server)); phase = 2; tick = 0; } }
				case 2 -> { if (tick >= 40) { server.execute(() -> measureInsert(server)); startTiming(server); phase = 3; tick = 0; } }
				case 3 -> { if (tick >= 200) { timing = false; Create.LOGGER.info("[harness] drawers chute tick: {} us/tick avg over {} ticks",
					String.format("%.1f", chuteNanos / 1000.0 / Math.max(1, chuteTicks)), chuteTicks); phase = 4; tick = 0; } }
				case 4 -> { if (tick >= 5) { Create.LOGGER.info("[harness] done"); done = true; mc.stop(); } }
				default -> {}
			}
		}

		private static BlockState block(String id) {
			return net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(net.minecraft.resources.ResourceLocation.parse(id)).defaultBlockState();
		}

		private static void build(MinecraftServer server) {
			ServerLevel level = server.overworld();
			level.setBlockAndUpdate(ACCESS, block("extended_drawers:access_point"));
			BlockState drawer = block("extended_drawers:quad_drawer");
			for (int x = 1; x <= WIDTH; x++)
				for (int y = 0; y < HEIGHT; y++)
					level.setBlockAndUpdate(ACCESS.offset(x, y, 0), drawer);
			level.setBlockAndUpdate(ACCESS.above(), AllBlocks.CHUTE.getDefaultState());
		}

		private static void fill(MinecraftServer server) {
			ServerLevel level = server.overworld();
			int slots = 0;
			try (var tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
				for (int x = 1; x <= WIDTH; x++)
					for (int y = 0; y < HEIGHT; y++) {
						var storage = net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.find(level, ACCESS.offset(x, y, 0), Direction.SOUTH);
						if (storage == null)
							continue;
						for (var view : storage)
							if (view instanceof net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant> slot) {
								slots++;
								if (slot.isResourceBlank())
									slot.insert(net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(net.minecraft.world.item.Items.STONE), 1, tx);
							}
					}
				tx.commit();
			}
			if (level.getBlockEntity(ACCESS.above()) instanceof com.simibubi.create.content.logistics.chute.ChuteBlockEntity chute)
				chute.setItem(new ItemStack(net.minecraft.world.item.Items.DIAMOND));
			Create.LOGGER.info("[harness] drawers filled {} drawer slots", slots);
		}

		private static void measureInsert(MinecraftServer server) {
			ServerLevel level = server.overworld();
			var raw = net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.find(level, ACCESS, Direction.UP);
			var handler = com.simibubi.create.foundation.transfer.capability.Capabilities.ItemHandler.BLOCK.find(level, ACCESS, Direction.UP);
			Create.LOGGER.info("[harness] drawers access point storage={} handler={} slots={}", raw == null ? null : raw.getClass().getName(),
				handler == null ? null : handler.getClass().getSimpleName(), handler == null ? -1 : handler.getSlots());
			if (handler == null)
				return;
			ItemStack diamond = new ItemStack(net.minecraft.world.item.Items.DIAMOND);
			TransferBenchmark.time("drawers insertItemStacked (diamond, access point)", () ->
				com.simibubi.create.foundation.transfer.item.ItemHandlerHelper.insertItemStacked(handler, diamond, true));
		}

		private static void startTiming(MinecraftServer server) {
			timing = true;
			net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.register(level -> {
				if (!timing || level != server.overworld())
					return;
				if (!(level.getBlockEntity(ACCESS.above()) instanceof com.simibubi.create.content.logistics.chute.ChuteBlockEntity chute))
					return;
				long start = System.nanoTime();
				chute.tick();
				chuteNanos += System.nanoTime() - start;
				chuteTicks++;
			});
		}
	}

	/**
	 * {@code -Dcreate.harness=crumble} (needs Extended Drawers in run/mods; the live crash also needs Sodium,
	 * ImmediatelyFast and Better Block Entities): an Extended Drawers drawer holding an item right in front of the
	 * player, with client-side block breaking progress cycling through every stage, as when mining it.
	 */
	private static final class CrumbleHarness {
		private static final BlockPos DRAWER = BASE.offset(0, 0, -3);

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			if (tick == 30) {
				MinecraftServer server = mc.getSingleplayerServer();
				server.execute(() -> {
					ServerLevel level = server.overworld();
					ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
					player.setGameMode(GameType.CREATIVE);
					BlockState drawer = net.minecraft.core.registries.BuiltInRegistries.BLOCK
						.get(net.minecraft.resources.ResourceLocation.parse("extended_drawers:quad_drawer")).defaultBlockState();
					// face the player (south), so its item is drawn
					for (var property : drawer.getProperties())
						if (property.getName().equals("facing") && property.getPossibleValues().contains(Direction.SOUTH))
							drawer = drawer.setValue((net.minecraft.world.level.block.state.properties.Property<Direction>) property, Direction.SOUTH);
					level.removeBlock(DRAWER, false);
					level.setBlockAndUpdate(DRAWER, drawer);
					// one of each item rendering path: plain, enchanted (glint), block, block entity item (chest)
					ItemStack sword = new ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
					sword.set(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
					ItemStack[] items = { new ItemStack(net.minecraft.world.item.Items.CHEST, 8), new ItemStack(net.minecraft.world.item.Items.DIAMOND, 32),
						sword, new ItemStack(net.minecraft.world.item.Items.STONE, 32) };
					var storage = net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.find(level, DRAWER, Direction.SOUTH);
					if (storage != null)
						try (var tx = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
							int slot = 0;
							for (var view : storage)
								if (slot < items.length && view instanceof net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant> single) {
									ItemStack stack = items[slot++];
									single.insert(net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(stack), stack.getCount(), tx);
								}
							tx.commit();
						}
					player.teleportTo(level, DRAWER.getX() + 0.5, DRAWER.getY(), DRAWER.getZ() + 2.0, 180, 25);
				});
			}
			if (tick > 60 && tick < 260) {
				int stage = (tick / 4) % 10;
				mc.level.destroyBlockProgress(mc.player.getId(), DRAWER, stage);
			}
			if (tick == 150)
				screenshot(mc, "crumble");
			if (tick == 260) {
				mc.level.destroyBlockProgress(mc.player.getId(), DRAWER, -1);
				Create.LOGGER.info("[harness] crumble done without crashing");
				Create.LOGGER.info("[harness] done");
				done = true;
				mc.stop();
			}
		}
	}

	/**
	 * {@code -Dcreate.harness=machines}: items on Create machines as players see them: a running belt fed with items,
	 * and a depot, a mechanical crafter and a deployer each given an item through their item handlers. Logs what the
	 * client-side block entities hold and takes screenshots.
	 */
	private static final class MachineItemsHarness {
		private static final BlockPos ORIGIN = BASE.offset(-30, 0, -20);
		private static final BlockPos BELT_START = ORIGIN, BELT_END = ORIGIN.offset(0, 0, -6);
		private static final BlockPos DEPOT = ORIGIN.offset(3, 0, -1), CRAFTER = ORIGIN.offset(3, 0, -3), DEPLOYER = ORIGIN.offset(3, 0, -5);

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			MinecraftServer server = mc.getSingleplayerServer();
			if (tick == 30)
				server.execute(() -> build(server));
			if (tick > 60 && tick < 200 && tick % 10 == 0)
				server.execute(() -> feed(server));
			if (tick == 70)
				server.execute(() -> fillMachines(server));
			if (tick == 150) {
				logClient(mc);
				screenshot(mc, "machines");
			}
			if (tick == 210) {
				Create.LOGGER.info("[harness] done");
				done = true;
				mc.stop();
			}
		}

		private static void build(MinecraftServer server) {
			ServerLevel level = server.overworld();
			ServerPlayer player = server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID());
			player.setGameMode(GameType.CREATIVE);
			for (int x = -2; x <= 5; x++)
				for (int z = -8; z <= 2; z++) {
					level.setBlockAndUpdate(ORIGIN.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
					for (int y = 0; y <= 3; y++)
						level.setBlockAndUpdate(ORIGIN.offset(x, y, z), Blocks.AIR.defaultBlockState());
				}
			BlockState shaft = AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X);
			level.setBlockAndUpdate(BELT_START, shaft);
			level.setBlockAndUpdate(BELT_END, shaft);
			com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem.createBelts(level, BELT_START, BELT_END);
			level.setBlockAndUpdate(BELT_START.west(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
			level.setBlockAndUpdate(DEPOT, AllBlocks.DEPOT.getDefaultState());
			level.setBlockAndUpdate(CRAFTER, AllBlocks.MECHANICAL_CRAFTER.getDefaultState());
			level.setBlockAndUpdate(DEPLOYER, AllBlocks.DEPLOYER.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP));
			player.teleportTo(level, ORIGIN.getX() + 1.5, ORIGIN.getY() + 4, ORIGIN.getZ() + 1.5, 160, 50);
		}

		private static void feed(MinecraftServer server) {
			var handler = Capabilities.ItemHandler.BLOCK.find(server.overworld(), BELT_START, Direction.UP);
			if (handler != null)
				handler.insertItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 4), false);
		}

		private static void fillMachines(MinecraftServer server) {
			ServerLevel level = server.overworld();
			for (BlockPos pos : new BlockPos[] { DEPOT, CRAFTER, DEPLOYER }) {
				var handler = Capabilities.ItemHandler.BLOCK.find(level, pos, Direction.UP);
				ItemStack rest = handler == null ? null : handler.insertItem(0, new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT), false);
				Create.LOGGER.info("[harness] machines insert into {}: handler={} rest={}", level.getBlockState(pos).getBlock(),
					handler == null ? null : handler.getClass().getSimpleName(), rest);
			}
		}

		private static void logClient(Minecraft mc) {
			var level = mc.level;
			int beltItems = 0;
			for (int z = 0; z >= -6; z--)
				if (level.getBlockEntity(BELT_START.offset(0, 0, z)) instanceof com.simibubi.create.content.kinetics.belt.BeltBlockEntity belt
					&& belt.isController() && belt.getInventory() != null)
					beltItems += belt.getInventory().getTransportedItems().size();
			Create.LOGGER.info("[harness] machines client: belt transported items={}", beltItems);
			for (BlockPos pos : new BlockPos[] { DEPOT, CRAFTER, DEPLOYER }) {
				var be = level.getBlockEntity(pos);
				var handler = be == null ? null : Capabilities.ItemHandler.BLOCK.find(level, pos, Direction.UP);
				Create.LOGGER.info("[harness] machines client: {} slot0={}", be == null ? null : be.getClass().getSimpleName(),
					handler == null ? null : handler.getStackInSlot(0));
			}
		}
	}

	/**
	 * {@code CREATE_HARNESS=gantry}: a 16-block gantry shaft along Z driven by a creative motor, a carriage with a plank
	 * tower on it. While the contraption moves, logs what the client knows about it (entity, bounds, bounding box,
	 * render readiness, Flywheel backend) and takes screenshots, for "gantry contraptions invisible while moving".
	 */
	private static final class GantryRenderHarness {
		private static final BlockPos ORIGIN = BASE.offset(-40, 0, 20);
		private static final int LENGTH = 16;

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			MinecraftServer server = mc.getSingleplayerServer();
			if (tick == 30)
				server.execute(() -> build(server));
			if (tick == 60)
				server.execute(() -> server.overworld().setBlockAndUpdate(ORIGIN.north(), AllBlocks.CREATIVE_MOTOR.getDefaultState()
					.setValue(BlockStateProperties.FACING, Direction.SOUTH)));
			if (tick >= 60 && tick <= 400 && tick % 20 == 0) {
				logClient(mc);
				screenshot(mc, "gantry-" + tick);
			}
			if (tick == 410) {
				Create.LOGGER.info("[harness] done");
				done = true;
				mc.stop();
			}
		}

		private static void build(MinecraftServer server) {
			ServerLevel level = server.overworld();
			ServerPlayer player = server.getPlayerList().getPlayer(Minecraft.getInstance().player.getUUID());
			player.setGameMode(GameType.CREATIVE);
			player.getAbilities().flying = true;
			player.onUpdateAbilities();
			level.setDayTime(6000);
			for (int x = -6; x <= 6; x++)
				for (int z = -3; z <= LENGTH + 3; z++) {
					level.setBlockAndUpdate(ORIGIN.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
					for (int y = 0; y <= 6; y++)
						level.setBlockAndUpdate(ORIGIN.offset(x, y, z), Blocks.AIR.defaultBlockState());
				}
			BlockState shaft = AllBlocks.GANTRY_SHAFT.getDefaultState().setValue(BlockStateProperties.FACING, Direction.SOUTH);
			for (int z = 0; z < LENGTH; z++)
				level.setBlockAndUpdate(ORIGIN.offset(0, 0, z), shaft);
			for (int z = 0; z < LENGTH; z++) {
				BlockPos pos = ORIGIN.offset(0, 0, z);
				level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Block.updateFromNeighbourShapes(level.getBlockState(pos), level, pos));
			}
			level.setBlockAndUpdate(ORIGIN.offset(0, 1, LENGTH - 2), AllBlocks.GANTRY_CARRIAGE.getDefaultState()
				.setValue(BlockStateProperties.FACING, Direction.UP)
				.setValue(com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE, true));
			// a copycat on the contraption: its block entity asks for a re-mesh while the contraption's visual is set up
			BlockPos copycat = ORIGIN.offset(0, 2, LENGTH - 2);
			level.setBlockAndUpdate(copycat, AllBlocks.COPYCAT_STEP.getDefaultState());
			if (level.getBlockEntity(copycat) instanceof CopycatBlockEntity cbe)
				cbe.setMaterial(Blocks.OAK_PLANKS.defaultBlockState());
			player.teleportTo(level, ORIGIN.getX() + 7.5, ORIGIN.getY() + 3, ORIGIN.getZ() + LENGTH / 2.0, 90, 10);
		}

		private static void logClient(Minecraft mc) {
			int found = 0;
			for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
				if (!(e instanceof com.simibubi.create.content.contraptions.AbstractContraptionEntity ce))
					continue;
				found++;
				var c = ce.getContraption();
				Create.LOGGER.info("[harness] gantry client t={}: {} id={} pos={} box={} bounds={} ready={} aliveOrStale={} shouldRender={} flywheel={} culled={}",
					tick, ce.getClass().getSimpleName(), ce.getId(), ce.position(), ce.getBoundingBox(), c == null ? null : c.bounds,
					ce.isReadyForRender(), ce.isAliveOrStale(), ce.shouldRender(mc.player.getX(), mc.player.getY(), mc.player.getZ()),
					dev.engine_room.flywheel.api.backend.Backend.REGISTRY.getId(dev.engine_room.flywheel.api.backend.BackendManager.currentBackend()), culledState(ce));
			}
			BlockPos carriage = null;
			for (int z = 0; z < LENGTH; z++)
				if (AllBlocks.GANTRY_CARRIAGE.has(mc.level.getBlockState(ORIGIN.offset(0, 1, z))))
					carriage = ORIGIN.offset(0, 1, z);
			Create.LOGGER.info("[harness] gantry client t={}: {} contraption entities, carriage block at {}", tick, found, carriage);
		}

		private static String culledState(net.minecraft.world.entity.Entity e) {
			try {
				Class<?> cullable = Class.forName("dev.tr7zw.entityculling.versionless.access.Cullable");
				if (!cullable.isInstance(e))
					return "n/a";
				return String.valueOf(cullable.getMethod("isCulled").invoke(e));
			} catch (ReflectiveOperationException ex) {
				return "no EntityCulling";
			}
		}
	}

	/**
	 * {@code CREATE_HARNESS=usekey}: a listener cancels every use-key interaction event (as Create does for a click on
	 * contraption controls) and counts them. A 2-tick click must fire once (it fired twice in one tick before
	 * 6.0.11-fabric.9: train controls on, then straight off); holding the key repeats every 4 ticks, as in vanilla/NeoForge.
	 */
	private static final class UseKeyHarness {
		private static final java.util.List<Integer> fired = new java.util.ArrayList<>();
		private static boolean armed;

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			var key = mc.options.keyUse.getKey();
			if (tick == 40) {
				com.simibubi.create.foundation.event.bus.CreateEventBus.BUS.addListener(com.simibubi.create.foundation.event.bus.EventPriority.HIGHEST, false,
					com.simibubi.create.foundation.event.client.InputEvent.InteractionKeyMappingTriggered.class, event -> {
						if (armed && event.isUseItem()) {
							fired.add(tick);
							event.setCanceled(true);
						}
					});
				armed = true;
			}
			if (tick == 60 || tick == 100) {
				net.minecraft.client.KeyMapping.set(key, true);
				net.minecraft.client.KeyMapping.click(key);
			}
			if (tick == 62 || tick == 113)
				net.minecraft.client.KeyMapping.set(key, false);
			if (tick == 90) {
				Create.LOGGER.info("[harness] usekey: 2-tick click fired {} time(s) at ticks {}: {}", fired.size(), fired, fired.size() == 1 ? "PASS" : "FAIL");
				fired.clear();
			}
			if (tick == 130) {
				Create.LOGGER.info("[harness] usekey: 13-tick hold fired {} time(s) at ticks {} (vanilla repeat: every 4 ticks): {}", fired.size(), fired,
					fired.size() == 4 ? "PASS" : "CHECK");
				done = true;
				mc.stop();
			}
		}
	}

	/**
	 * {@code CREATE_HARNESS=emipotion} (run with -PwithEmi): once EMI has loaded, counts the recipes EMI lists for one
	 * potion fluid (Swiftness) and for the Swiftness potion item, by output and by input, then opens the fluid's recipes.
	 */
	private static final class EmiPotionHarness {
		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			if (tick < 100 || tick % 20 != 0)
				return;
			var manager = dev.emi.emi.api.EmiApi.getRecipeManager();
			if (phase == 0) {
				if (manager.getRecipes().isEmpty() && tick < 1200)
					return;
				var contents = new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.SWIFTNESS);
				var fluid = com.simibubi.create.compat.emi.EmiLayout.toEmi(com.simibubi.create.content.fluids.potion.PotionFluid.of(1000, contents,
					com.simibubi.create.content.fluids.potion.PotionFluid.BottleType.REGULAR));
				ItemStack potionItem = new ItemStack(net.minecraft.world.item.Items.POTION);
				potionItem.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, contents);
				var item = dev.emi.emi.api.stack.EmiStack.of(potionItem);
				Create.LOGGER.info("[harness] emipotion: {} recipes loaded; Swiftness fluid: {} recipes make it, {} use it; Swiftness potion: {} make it, {} use it",
					manager.getRecipes().size(), manager.getRecipesByOutput(fluid).size(), manager.getRecipesByInput(fluid).size(),
					manager.getRecipesByOutput(item).size(), manager.getRecipesByInput(item).size());
				for (var r : manager.getRecipesByOutput(fluid))
					Create.LOGGER.info("[harness] emipotion:   makes the fluid: {} ({})", r.getId(), r.getCategory().getId());
				dev.emi.emi.api.EmiApi.displayRecipes(fluid);
				phase = 1;
				tick = 0;
				return;
			}
			if (tick >= 40) {
				screenshot(mc, "emipotion");
				done = true;
				mc.stop();
			}
		}
	}

	/**
	 * {@code -Dcreate.harness=opac} (needs Open Parties and Claims in run/mods): checks which of OPAC's Create
	 * protection mixins applied, claims a chunk for another (made-up) player, then 1) a powered mechanical drill outside
	 * the claim faces a stone block inside it, 2) a contraption anchored outside the claim is put back into the world
	 * with blocks over stone inside the claim (the "saved contraption disassembled into a claim" grief). Logs what is
	 * left in the claim. Compare a run with {@code -Dcreate.opacCompat=false}.
	 */
	private static final class OpacHarness {
		private static final String[] TARGETS = {
			"com.simibubi.create.content.contraptions.AbstractContraptionEntity",
			"com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer",
			"com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity",
			"com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint",
			"com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity",
			"com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour",
			"com.simibubi.create.content.contraptions.Contraption",
			"com.simibubi.create.content.contraptions.ContraptionCollider",
			"com.simibubi.create.content.contraptions.minecart.CouplingHandler",
			"com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity",
			"com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour",
			"com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour",
			"com.simibubi.create.content.kinetics.base.KineticBlockEntity",
			"com.simibubi.create.content.contraptions.mounted.MinecartContraptionItem",
			"com.simibubi.create.content.contraptions.actors.plough.PloughMovementBehaviour",
			"com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity",
			"com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem",
			// this port's own redo of OPAC's NeoForge-only mixins (mixin/compat/opac)
			"com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket",
			"com.simibubi.create.content.equipment.clipboard.ClipboardEditPacket",
			"com.simibubi.create.content.contraptions.sync.ContraptionInteractionPacket",
			"com.simibubi.create.content.contraptions.actors.trainControls.ControlsInputPacket",
			"com.simibubi.create.content.contraptions.elevator.ElevatorTargetFloorPacket",
			"com.simibubi.create.content.fluids.OpenEndedPipe",
			"com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity",
			"com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket",
			"com.simibubi.create.content.contraptions.glue.SuperGlueRemovalPacket",
			"com.simibubi.create.content.contraptions.glue.SuperGlueSelectionPacket",
			"com.simibubi.create.content.equipment.toolbox.ToolboxDisposeAllPacket",
			"com.simibubi.create.content.equipment.toolbox.ToolboxEquipPacket",
			"com.simibubi.create.content.trains.entity.TrainRelocationPacket" };
		private static final java.util.UUID OTHER = java.util.UUID.fromString("00000000-0000-0000-0000-00000000beef");
		private static BlockPos border;

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			MinecraftServer server = mc.getSingleplayerServer();
			try {
				if (tick == 20)
					server.execute(OpacHarness::checkMixins);
				if (tick == 30)
					server.execute(() -> setup(server));
				if (tick == 50)
					server.execute(() -> disassembleIntoClaim(server));
				if (tick == 60)
					server.execute(() -> {
						ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
						player.teleportTo(server.overworld(), border.getX() - 1.5, border.getY(), border.getZ() + 5.5, -90, 0);
					});
				if (tick == 80)
					server.execute(() -> {
						ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
						new com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket(border.offset(1, 0, 5),
							AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z)).handle(player);
					});
				if (tick == 160)
					server.execute(() -> report(server));
				if (tick == 170) {
					Create.LOGGER.info("[harness] done");
					done = true;
					mc.stop();
				}
			} catch (Throwable t) {
				Create.LOGGER.error("[harness] opac failed", t);
				done = true;
				mc.stop();
			}
		}

		/** Counts, per target class, the members Mixin merged in from OPAC's mixins ({@code @MixinMerged}). */
		private static void checkMixins() {
			int applied = 0;
			for (String target : TARGETS) {
				try {
					Class<?> type = Class.forName(target, false, OpacHarness.class.getClassLoader());
					java.util.Set<String> opac = new java.util.TreeSet<>();
					for (var method : type.getDeclaredMethods()) {
						var merged = method.getAnnotation(org.spongepowered.asm.mixin.transformer.meta.MixinMerged.class);
						if (merged != null && (merged.mixin().startsWith("xaero.pac") || merged.mixin().contains(".mixin.compat.opac.")))
							opac.add(merged.mixin().substring(merged.mixin().lastIndexOf('.') + 1));
					}
					for (Class<?> itf : type.getInterfaces())
						if (itf.getName().startsWith("xaero.pac"))
							opac.add("implements " + itf.getSimpleName());
					if (!opac.isEmpty())
						applied++;
					Create.LOGGER.info("[harness] opac mixins on {}: {}", target.substring(target.lastIndexOf('.') + 1), opac);
				} catch (Throwable t) {
					Create.LOGGER.error("[harness] opac mixins on {} FAILED", target, t);
				}
			}
			Create.LOGGER.info("[harness] opac: {} of {} target classes carry OPAC mixins", applied, TARGETS.length);
		}

		private static void setup(MinecraftServer server) {
			ServerLevel level = server.overworld();
			// a chunk boundary near the test area: x = border.getX() is the first column of the claimed chunk
			int chunkX = (BASE.getX() - 80) >> 4, chunkZ = (BASE.getZ() + 64) >> 4;
			border = new BlockPos((chunkX + 1) << 4, BASE.getY(), (chunkZ << 4) + 4);
			Object claim;
			try {
				Object api = Class.forName("xaero.pac.common.server.api.OpenPACServerAPI").getMethod("get", MinecraftServer.class).invoke(null, server);
				Object claims = api.getClass().getMethod("getServerClaimsManager").invoke(api);
				java.lang.reflect.Method claimMethod = null;
				for (var m : claims.getClass().getMethods())
					if (m.getName().equals("claim") && m.getParameterCount() == 6)
						claimMethod = m;
				claim = claimMethod.invoke(claims, level.dimension().location(), OTHER, 0, chunkX + 1, chunkZ, false);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
			Create.LOGGER.info("[harness] opac: claimed chunk {},{} for {}: {}", chunkX + 1, chunkZ, OTHER, claim);
			for (int dx = -6; dx <= 6; dx++)
				for (int dz = -1; dz <= 5; dz++) {
					level.setBlockAndUpdate(border.offset(dx, -1, dz), Blocks.SMOOTH_STONE.defaultBlockState());
					for (int y = 0; y <= 2; y++)
						level.setBlockAndUpdate(border.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
				}
			// 1) drill outside the claim, facing a stone block just inside it
			BlockPos drill = border.offset(-1, 0, 3);
			level.setBlockAndUpdate(drill.west(), AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
			level.setBlockAndUpdate(drill, AllBlocks.MECHANICAL_DRILL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.EAST));
			level.setBlockAndUpdate(border.offset(0, 0, 3), Blocks.STONE.defaultBlockState());
			// 3) a shaft inside the claim, for the wrench menu packet
			level.setBlockAndUpdate(border.offset(1, 0, 5), AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X));
			// 2) stone inside the claim where the contraption will land
			for (int dx = 0; dx < 4; dx++)
				level.setBlockAndUpdate(border.offset(dx, 0, 0), Blocks.STONE.defaultBlockState());
		}

		private static void disassembleIntoClaim(MinecraftServer server) {
			ServerLevel level = server.overworld();
			var contraption = new com.simibubi.create.content.contraptions.bearing.BearingContraption(false, Direction.UP);
			BlockPos anchor = border.offset(-3, 0, 0);
			contraption.anchor = anchor;
			contraption.getStorage().initialize();
			for (int dx = 0; dx < 7; dx++) {
				BlockPos local = new BlockPos(dx, 0, 0);
				contraption.getBlocks().put(local, new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo(
					local, Blocks.OAK_PLANKS.defaultBlockState(), null));
			}
			contraption.addBlocksToWorld(level, new com.simibubi.create.content.contraptions.StructureTransform(anchor, Direction.Axis.Y,
				net.minecraft.world.level.block.Rotation.NONE, net.minecraft.world.level.block.Mirror.NONE));
		}

		private static void report(MinecraftServer server) {
			ServerLevel level = server.overworld();
			StringBuilder line = new StringBuilder();
			for (int dx = -3; dx < 4; dx++)
				line.append(dx < 0 ? " out:" : " IN:").append(level.getBlockState(border.offset(dx, 0, 0)).getBlock().getName().getString());
			Create.LOGGER.info("[harness] opac contraption put back (x -3..3, claim from 0):{}", line);
			Create.LOGGER.info("[harness] opac drill target inside the claim after 110 ticks: {} (drill speed {})",
				level.getBlockState(border.offset(0, 0, 3)).getBlock().getName().getString(),
				level.getBlockEntity(border.offset(-1, 0, 3)) instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity k ? k.getSpeed() : 0);
			Create.LOGGER.info("[harness] opac wrench menu packet on a shaft inside the claim: axis now {} (was X)",
				level.getBlockState(border.offset(1, 0, 5)).getOptionalValue(BlockStateProperties.AXIS).orElse(null));
			Create.LOGGER.info("[harness] opac compat {}", "false".equals(System.getenv("CREATE_OPAC_COMPAT")) ? "OFF" : "on");
		}
	}

	/**
	 * {@code -Dcreate.harness=keys}: checks that Create's conflict-safe Shift/Ctrl/Alt modifiers don't take keys from
	 * vanilla mappings. Runs sneak/sprint once swapped (sneak on Left Ctrl, sprint on Left Shift) and once at the
	 * defaults, pressing keys through vanilla's key callback, and logs which mapping owns each key. Restores the bindings
	 * before quitting (not saved to options.txt either way).
	 */
	private static final class KeyHarness {
		private static final int CTRL = org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL, SHIFT = org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT,
			ALT = org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT, W = org.lwjgl.glfw.GLFW.GLFW_KEY_W;
		private static net.minecraft.client.KeyMapping sneak, sprint;
		private static com.mojang.blaze3d.platform.InputConstants.Key sneakKey, sprintKey;
		private static int failures;

		static void tick(Minecraft mc) {
			if (done || mc.level == null || mc.player == null || mc.getSingleplayerServer() == null)
				return;
			mc.options.pauseOnLostFocus = false;
			tick++;
			try {
				switch (phase) {
					case 0 -> { if (tick >= 40) { setup(mc); phase = 1; tick = 0; } }
					// swapped: sneak on Left Ctrl, sprint on Left Shift
					case 1 -> { if (tick >= 20) { bind(mc, CTRL, SHIFT); logOwners(mc, "swapped"); press(mc, CTRL, true); phase = 2; tick = 0; } }
					case 2 -> { if (tick >= 5) { check("swapped: sneak (Left Ctrl) isDown", sneak.isDown()); check("swapped: player crouching", mc.player.isShiftKeyDown()); press(mc, CTRL, false); press(mc, W, true); press(mc, SHIFT, true); phase = 3; tick = 0; } }
					case 3 -> { if (tick >= 10) { check("swapped: sprint (Left Shift) isDown", sprint.isDown()); logPlayer(mc, "swapped"); check("swapped: player sprinting", mc.player.isSprinting()); press(mc, SHIFT, false); press(mc, W, false); controls(mc); phase = 4; tick = 0; } }
					case 4 -> { if (tick >= 20) { screenshot(mc, "keys-controls-swapped"); mc.setScreen(null); phase = 5; tick = 0; } }
					// defaults: sneak on Left Shift, sprint on Left Ctrl
					case 5 -> { if (tick >= 20) { bind(mc, SHIFT, CTRL); logOwners(mc, "default"); press(mc, SHIFT, true); phase = 6; tick = 0; } }
					case 6 -> { if (tick >= 5) { check("default: sneak (Left Shift) isDown", sneak.isDown()); check("default: player crouching", mc.player.isShiftKeyDown()); press(mc, SHIFT, false); press(mc, W, true); press(mc, CTRL, true); phase = 7; tick = 0; } }
					case 7 -> { if (tick >= 10) { check("default: sprint (Left Ctrl) isDown", sprint.isDown()); logPlayer(mc, "default"); check("default: player sprinting", mc.player.isSprinting()); press(mc, CTRL, false); press(mc, W, false); controls(mc); phase = 8; tick = 0; } }
					case 8 -> { if (tick >= 20) { screenshot(mc, "keys-controls-default"); mc.setScreen(null); finish(mc); } }
					default -> {}
				}
			} catch (Throwable t) {
				Create.LOGGER.error("[harness] failed", t);
				finish(mc);
			}
		}

		private static void setup(Minecraft mc) {
			sneak = mc.options.keyShift;
			sprint = mc.options.keySprint;
			sneakKey = sneak.getKey();
			sprintKey = sprint.getKey();
			MinecraftServer server = mc.getSingleplayerServer();
			server.execute(() -> {
				ServerLevel level = server.overworld();
				ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
				level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING).set(false, server);
				for (var entity : level.getAllEntities())
					if (!(entity instanceof net.minecraft.world.entity.player.Player))
						entity.discard();
				player.setGameMode(GameType.SURVIVAL);
				player.getFoodData().setFoodLevel(20);
				level.setDayTime(6000);
				for (int x = -3; x <= 3; x++)
					for (int z = -12; z <= 3; z++) {
						level.setBlockAndUpdate(BASE.offset(x, -1, z), Blocks.SMOOTH_STONE.defaultBlockState());
						for (int y = 0; y <= 2; y++)
							level.setBlockAndUpdate(BASE.offset(x, y, z), Blocks.AIR.defaultBlockState());
					}
				player.teleportTo(level, 0.5, 150.0, 2.5, 180, 0);
			});
		}

		private static void bind(Minecraft mc, int sneakGlfw, int sprintGlfw) {
			sneak.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(sneakGlfw));
			sprint.setKey(com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(sprintGlfw));
			net.minecraft.client.KeyMapping.resetMapping();
			MinecraftServer server = mc.getSingleplayerServer();
			server.execute(() -> server.getPlayerList().getPlayer(mc.player.getUUID())
				.teleportTo(server.overworld(), 0.5, 150.0, 2.5, 180, 0));
		}

		private static void press(Minecraft mc, int glfwKey, boolean down) {
			mc.keyboardHandler.keyPress(mc.getWindow().getWindow(), glfwKey, 0,
				down ? org.lwjgl.glfw.GLFW.GLFW_PRESS : org.lwjgl.glfw.GLFW.GLFW_RELEASE, 0);
		}

		private static void controls(Minecraft mc) {
			mc.setScreen(new net.minecraft.client.gui.screens.options.controls.KeyBindsScreen(null, mc.options));
		}

		@SuppressWarnings("unchecked")
		private static void logOwners(Minecraft mc, String scenario) throws ReflectiveOperationException {
			var field = net.minecraft.client.KeyMapping.class.getDeclaredField("MAP");
			field.setAccessible(true);
			var map = (java.util.Map<com.mojang.blaze3d.platform.InputConstants.Key, net.minecraft.client.KeyMapping>) field.get(null);
			for (int glfw : new int[] { CTRL, SHIFT, ALT }) {
				var key = com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM.getOrCreate(glfw);
				var owner = map.get(key);
				List<String> sharing = new java.util.ArrayList<>(), conflicts = new java.util.ArrayList<>();
				for (var mapping : mc.options.keyMappings) {
					if (!mapping.getKey().equals(key))
						continue;
					sharing.add(mapping.getName());
					for (var other : mc.options.keyMappings)
						if (other != mapping && mapping.same(other))
							conflicts.add(mapping.getName() + "~" + other.getName());
				}
				Create.LOGGER.info("[harness] keys {}: {} owner={} bound={} shownConflicts={}", scenario, key.getName(),
					owner == null ? null : owner.getName(), sharing, conflicts);
			}
			for (var mapping : mc.options.keyMappings)
				if (mapping instanceof net.createmod.catnip.client.ConflictSafeKeyMapping && map.containsValue(mapping))
					check(scenario + ": " + mapping.getName() + " kept out of MAP", false);
			Create.LOGGER.info("[harness] keys {}: ctrlDown={} shiftDown={} altDown={} toolbelt={} (nothing physically held)", scenario,
				com.simibubi.create.AllKeys.ctrlDown(), com.simibubi.create.AllKeys.shiftDown(), com.simibubi.create.AllKeys.altDown(),
				com.simibubi.create.AllKeys.TOOLBELT.isPressed());
		}

		private static void logPlayer(Minecraft mc, String scenario) {
			var p = mc.player;
			Create.LOGGER.info("[harness] keys {}: forward={} impulse={} onGround={} food={} mode={} sprinting={} crouching={} pos={}",
				scenario, mc.options.keyUp.isDown(), p.input.forwardImpulse, p.onGround(), p.getFoodData().getFoodLevel(),
				mc.gameMode.getPlayerMode(), p.isSprinting(), p.isShiftKeyDown(), p.position());
		}

		private static void check(String what, boolean ok) {
			if (!ok)
				failures++;
			Create.LOGGER.info("[harness] keys {} {}", ok ? "PASS" : "FAIL", what);
		}

		private static void finish(Minecraft mc) {
			if (sneak != null) {
				sneak.setKey(sneakKey);
				sprint.setKey(sprintKey);
				net.minecraft.client.KeyMapping.resetMapping();
			}
			Create.LOGGER.info("[harness] keys done, failures={}", failures);
			Create.LOGGER.info("[harness] done");
			done = true;
			mc.stop();
		}
	}

	/** Separate class so EMI classes only load when EMI is present. */
	private static final class EmiHarness {
		static boolean show(Minecraft mc, int index) {
			List<dev.emi.emi.api.recipe.EmiRecipeCategory> categories = dev.emi.emi.api.EmiApi.getRecipeManager().getCategories().stream()
				.filter(c -> c.getId().getNamespace().equals(Create.ID)).toList();
			Create.LOGGER.info("[harness] EMI create categories: {}", categories.stream().map(c -> c.getId().getPath()).toList());
			if (categories.size() <= index)
				return false;
			dev.emi.emi.api.EmiApi.displayRecipeCategory(categories.get(index));
			return true;
		}
	}

	/** Separate class so the map mods' classes only load when they are present. */
	private static final class MapHarness {
		static boolean openXaero(Minecraft mc) {
			try {
				Object map = new xaero.map.gui.GuiMap(mc.screen, mc.screen,
					xaero.map.WorldMapSession.getCurrentSession().getMapProcessor(), mc.player);
				mc.setScreen((net.minecraft.client.gui.screens.Screen) map);
				return true;
			} catch (Throwable e) {
				Create.LOGGER.error("[harness] could not open Xaero's world map", e);
				return false;
			}
		}

		static boolean openJourneyMap(Minecraft mc) {
			try {
				mc.setScreen(new journeymap.client.ui.fullscreen.Fullscreen());
				return true;
			} catch (Throwable e) {
				Create.LOGGER.error("[harness] could not open JourneyMap's fullscreen map", e);
				return false;
			}
		}

		static boolean openFtbChunks(Minecraft mc) {
			try {
				return dev.ftb.mods.ftbchunks.client.gui.LargeMapScreen.openMap();
			} catch (Throwable e) {
				Create.LOGGER.error("[harness] could not open FTB Chunks' large map", e);
				return false;
			}
		}
	}

	/** Separate class so JEI classes only load when JEI is present. */
	private static final class JeiHarness {
		static boolean showRecipes(Minecraft mc) {
			mezz.jei.api.runtime.IJeiRuntime runtime = com.simibubi.create.compat.jei.CreateJEI.runtime;
			if (runtime == null)
				return false;
			runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT,
				mezz.jei.api.constants.VanillaTypes.ITEM_STACK, AllItems.ANDESITE_ALLOY.asStack()));
			return true;
		}

		static void next(Minecraft mc) {
			mezz.jei.api.runtime.IJeiRuntime runtime = com.simibubi.create.compat.jei.CreateJEI.runtime;
			java.util.List<mezz.jei.api.recipe.RecipeType<?>> types = new java.util.ArrayList<>();
			runtime.getRecipeManager().createRecipeCategoryLookup().get().forEach(c -> {
				if (c.getRecipeType().getUid().getNamespace().equals(Create.ID))
					types.add(c.getRecipeType());
			});
			Create.LOGGER.info("[harness] JEI create categories: {}", types.stream().map(t -> t.getUid().getPath()).toList());
			runtime.getRecipesGui().showTypes(types);
		}
	}

	/** Server-side {@code openMenu} of the block entity at pos (exercises the extended-data menu path). */
	private static void openMenuAt(Minecraft mc, BlockPos pos) {
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
			if (server.overworld().getBlockEntity(pos) instanceof net.minecraft.world.MenuProvider provider
				&& provider instanceof com.simibubi.create.foundation.blockEntity.SmartBlockEntity sbe)
				player.openMenu(provider, sbe::sendToMenu);
			else
				Create.LOGGER.warn("[harness] no menu provider at {}", pos);
		});
	}

	/** Breaks a block the way a player does (ServerPlayerGameMode#destroyBlock). */
	private static void breakBlockAsPlayer(Minecraft mc, BlockPos pos) {
		MinecraftServer server = mc.getSingleplayerServer();
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
			Create.LOGGER.info("[harness] breaking {} at {}", server.overworld().getBlockState(pos).getBlock().getName().getString(), pos);
			player.gameMode.destroyBlock(pos);
		});
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
			// a short stretch of train track, so the train map overlay (compat/trainmap) has a network to draw
			for (int x = -2; x <= 25; x++)
				level.setBlockAndUpdate(BASE.offset(x, 0, 12),
					AllBlocks.TRACK.getDefaultState().setValue(com.simibubi.create.content.trains.track.TrackBlock.SHAPE,
						com.simibubi.create.content.trains.track.TrackShape.XO));
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
			place.accept(new Object[] { 24, 0, 12, AllBlocks.SCHEMATIC_TABLE.getDefaultState() });
			place.accept(new Object[] { 24, 0, 11, AllBlocks.SCHEMATICANNON.getDefaultState() });
			place.accept(new Object[] { 22, 0, 9, AllBlocks.MECHANICAL_BEARING.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP) });
			place.accept(new Object[] { 22, 1, 9, Blocks.OAK_PLANKS.defaultBlockState() });
			// second bearing carrying a connected-texture casing: a non-vanilla-adapter model in the contraption buffer
			// (Flywheel-off path, ShadedBlockSbbBuilder)
			place.accept(new Object[] { 0, -1, -1, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP) });
			place.accept(new Object[] { 0, 0, -1, AllBlocks.MECHANICAL_BEARING.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP) });
			place.accept(new Object[] { 0, 1, -1, AllBlocks.ANDESITE_CASING.getDefaultState() });
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
