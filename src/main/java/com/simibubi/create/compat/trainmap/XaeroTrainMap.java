package com.simibubi.create.compat.trainmap;

/**
 * Fabric port stub: the XaeroTrainMap map-mod integration needs that mod's (Fabric) API as a compile dependency —
 * TODO(Phase 12). All hooks are no-ops until then; the map-independent train sync still works.
 */
public class XaeroTrainMap {
	public static void tick() {}
	public static void mouseClick(Object event) {}
	public static void cancelTooltips(Object event) {}
	public static void renderGui(Object event) {}
	public static boolean isMapOpen(net.minecraft.client.gui.screens.Screen screen) { return false; }
	public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getRenderedDimension() { return net.minecraft.world.level.Level.OVERWORLD; }
}
