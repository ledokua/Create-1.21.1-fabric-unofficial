package com.simibubi.create.foundation.event.client;

import net.minecraft.resources.ResourceLocation;

/** Ids of vanilla HUD layers, as upstream names them; only used as ordering hints by {@link RegisterGuiLayersEvent}. */
public final class VanillaGuiLayers {

	private VanillaGuiLayers() {}

	private static ResourceLocation vanilla(String path) {
		return ResourceLocation.withDefaultNamespace(path);
	}

	public static final ResourceLocation CAMERA_OVERLAYS = vanilla("camera_overlays");
	public static final ResourceLocation CROSSHAIR = vanilla("crosshair");
	public static final ResourceLocation HOTBAR = vanilla("hotbar");
	public static final ResourceLocation JUMP_METER = vanilla("jump_meter");
	public static final ResourceLocation EXPERIENCE_BAR = vanilla("experience_bar");
	public static final ResourceLocation PLAYER_HEALTH = vanilla("player_health");
	public static final ResourceLocation ARMOR_LEVEL = vanilla("armor_level");
	public static final ResourceLocation FOOD_LEVEL = vanilla("food_level");
	public static final ResourceLocation VEHICLE_HEALTH = vanilla("vehicle_health");
	public static final ResourceLocation AIR_LEVEL = vanilla("air_level");
	public static final ResourceLocation SELECTED_ITEM_NAME = vanilla("selected_item_name");
	public static final ResourceLocation SPECTATOR_TOOLTIP = vanilla("spectator_tooltip");
	public static final ResourceLocation EXPERIENCE_LEVEL = vanilla("experience_level");
	public static final ResourceLocation EFFECTS = vanilla("effects");
	public static final ResourceLocation BOSS_OVERLAY = vanilla("boss_overlay");
	public static final ResourceLocation SLEEP_OVERLAY = vanilla("sleep_overlay");
	public static final ResourceLocation DEMO_OVERLAY = vanilla("demo_overlay");
	public static final ResourceLocation DEBUG_OVERLAY = vanilla("debug_overlay");
	public static final ResourceLocation SCOREBOARD_SIDEBAR = vanilla("scoreboard_sidebar");
	public static final ResourceLocation OVERLAY_MESSAGE = vanilla("overlay_message");
	public static final ResourceLocation TITLE = vanilla("title");
	public static final ResourceLocation CHAT = vanilla("chat");
	public static final ResourceLocation TAB_LIST = vanilla("tab_list");
	public static final ResourceLocation SUBTITLE_OVERLAY = vanilla("subtitle_overlay");
	public static final ResourceLocation SAVING_INDICATOR = vanilla("saving_indicator");
}
