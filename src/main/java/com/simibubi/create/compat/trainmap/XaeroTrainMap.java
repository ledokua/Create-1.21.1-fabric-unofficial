package com.simibubi.create.compat.trainmap;

import java.util.List;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.event.client.InputEvent;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.foundation.mixin.compat.xaeros.XaeroFullscreenMapAccessor;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import xaero.map.gui.GuiMap;

/**
 * Train map overlay on Xaero's World Map. Fabric differences from upstream: the map screens are passed around as
 * {@link Object}/{@link Screen} because Xaero's own screen base class lives in its bundled xaerolib jar (jar-in-jar,
 * not on the compile classpath), and the "map open" check reads the public {@code parent} field reflectively for the
 * same reason. Rendering is driven by {@code foundation.mixin.compat.xaeros.XaeroFullscreenMapMixin}.
 */
@Environment(EnvType.CLIENT)
public class XaeroTrainMap {
	private static boolean requesting;
	private static ResourceKey<Level> renderedDimension;
	private static boolean encounteredException = false;

	public static void tick() {
		if (!AllConfigs.client().showTrainMapOverlay.get() || !isMapOpen(Minecraft.getInstance().screen)) {
			if (requesting)
				TrainMapSyncClient.stopRequesting();
			requesting = false;
			return;
		}
		TrainMapManager.tick();
		requesting = true;
		TrainMapSyncClient.requestData();
	}

	public static void mouseClick(InputEvent.MouseButton.Pre event) {
		if (encounteredException)
			return;

		Minecraft mc = Minecraft.getInstance();
		if (!isFullscreenMap(mc.screen))
			return;

		Window window = mc.getWindow();
		double mX = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
		double mY = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();

		if (TrainMapManager.handleToggleWidgetClick(Mth.floor(mX), Mth.floor(mY), 3, 30))
			event.setCanceled(true);
	}

	// Called by XaeroFullscreenMapMixin, guarded by try-catch
	public static void onRender(GuiGraphics graphics, Object mapScreen, int mX, int mY, float pt) {
		Screen screen = (Screen) mapScreen;
		XaeroFullscreenMapAccessor map = (XaeroFullscreenMapAccessor) mapScreen;

		double x = map.create$getCameraX();
		double z = map.create$getCameraZ();
		double mapScale = map.create$getScale();
		renderedDimension = map.create$getMapProcessor()
			.getMapWorld()
			.getCurrentDimension()
			.getDimId();

		if (!AllConfigs.client().showTrainMapOverlay.get()) {
			renderToggleWidgetAndTooltip(graphics, screen, mX, mY);
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		Window window = mc.getWindow();

		double guiScale = (double) window.getScreenWidth() / window.getGuiScaledWidth();
		double interfaceScale = (double) window.getWidth() / window.getScreenWidth();
		double scale = mapScale / guiScale / interfaceScale;

		PoseStack pose = graphics.pose();
		pose.pushPose();

		pose.translate(screen.width / 2.0f, screen.height / 2.0f, 0);
		pose.scale((float) scale, (float) scale, 1);
		pose.translate(-x, -z, 0);

		float mouseX = mX - screen.width / 2.0f;
		float mouseY = mY - screen.height / 2.0f;
		mouseX /= scale;
		mouseY /= scale;
		mouseX += x;
		mouseY += z;

		Rect2i bounds =
			new Rect2i(Mth.floor(-screen.width / 2.0f / scale + x), Mth.floor(-screen.height / 2.0f / scale + z),
				Mth.floor(screen.width / scale), Mth.floor(screen.height / scale));

		List<FormattedText> tooltip =
			TrainMapManager.renderAndPick(graphics, Mth.floor(mouseX), Mth.floor(mouseY), false, bounds);

		pose.popPose();

		if (!renderToggleWidgetAndTooltip(graphics, screen, mX, mY) && tooltip != null)
			RemovedGuiUtils.drawHoveringText(graphics, tooltip, mX, mY, screen.width, screen.height, 256, mc.font);
	}

	private static boolean renderToggleWidgetAndTooltip(GuiGraphics graphics, Screen screen, int mouseX, int mouseY) {
		TrainMapManager.renderToggleWidget(graphics, 3, 30);
		if (!TrainMapManager.isToggleWidgetHovered(mouseX, mouseY, 3, 30))
			return false;

		RemovedGuiUtils.drawHoveringText(graphics, List.of(CreateLang.translate("train_map.toggle")
			.component()), mouseX, mouseY + 20, screen.width, screen.height, 256, Minecraft.getInstance().font);
		return true;
	}

	public static ResourceKey<Level> getRenderedDimension() {
		return renderedDimension;
	}

	/** True while the fullscreen map itself, or one of the screens opened on top of it, is showing. */
	public static boolean isMapOpen(Screen screen) {
		if (encounteredException || screen == null)
			return false;

		try {
			if (isFullscreenMap(screen))
				return true;
			// xaero.lib.client.gui.ScreenBase#parent — public field of a class Create does not compile against
			return isFullscreenMap(parentOf(screen));
		} catch (Throwable e) {
			Create.LOGGER.error("Failed to check if Xaero's World Map was open for train map integration:", e);
			encounteredException = true;
			return false;
		}
	}

	private static boolean isFullscreenMap(Object screen) {
		return screen instanceof GuiMap;
	}

	private static Object parentOf(Screen screen) {
		try {
			return screen.getClass()
				.getField("parent")
				.get(screen);
		} catch (NoSuchFieldException | IllegalAccessException | SecurityException e) {
			return null;
		}
	}
}
