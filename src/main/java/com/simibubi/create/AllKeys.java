package com.simibubi.create;

import java.util.function.BiConsumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.client.ConflictSafeKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import net.fabricmc.api.EnvType;
import com.simibubi.create.foundation.event.bus.SubscribeEvent;
import com.simibubi.create.foundation.event.bus.EventBusSubscriber;
import com.simibubi.create.foundation.event.client.RegisterKeyMappingsEvent;

@EventBusSubscriber(EnvType.CLIENT)
public enum AllKeys {

	TOOL_MENU("toolmenu", GLFW.GLFW_KEY_LEFT_ALT, "Focus Schematic Overlay"),
	ACTIVATE_TOOL(GLFW.GLFW_KEY_LEFT_CONTROL),
	TOOLBELT("toolbelt", GLFW.GLFW_KEY_LEFT_ALT, "Access Nearby Toolboxes"),
	ROTATE_MENU("rotate_menu", GLFW.GLFW_KEY_UNKNOWN, "Open Block Rotation Menu"),

	SHIFT_MODIFIER("shift_modifier", GLFW.GLFW_KEY_LEFT_SHIFT, "Shift Modifier", true),
	CTRL_MODIFIER("ctrl_modifier", GLFW.GLFW_KEY_LEFT_CONTROL, "Ctrl Modifier", true),
	ALT_MODIFIER("alt_modifier", GLFW.GLFW_KEY_LEFT_ALT, "Alt Modifier", true),
	;

	private KeyMapping keybind;
	private final String description;
	private final String translation;
	private final int key;
	private final boolean modifiable;
	private final boolean conflictSafe;

	AllKeys(int defaultKey) {
		this("", defaultKey, "");
	}

	AllKeys(String description, int defaultKey, String translation) {
		this(description, defaultKey, translation, false);
	}

	AllKeys(String description, int defaultKey, String translation, boolean conflictSafe) {
		this.description = Create.ID + ".keyinfo." + description;
		this.key = defaultKey;
		this.modifiable = !description.isEmpty();
		this.translation = translation;
		this.conflictSafe = conflictSafe;
	}

	public static void provideLang(BiConsumer<String, String> consumer) {
		for (AllKeys key : values())
			if (key.modifiable)
				consumer.accept(key.description, key.translation);
	}

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		for (AllKeys key : values()) {
			// Fabric port: unregistered keys (activate_tool, read from the raw key state) are made conflict-safe too, as
			// any KeyMapping enters vanilla's one-mapping-per-key table on construction and could take e.g. sprint's key.
			if (key.conflictSafe || !key.modifiable) {
				key.keybind = new ConflictSafeKeyMapping(key.description, key.key, Create.NAME);
			} else {
				key.keybind = new KeyMapping(key.description, key.key, Create.NAME);
			}
			if (!key.modifiable)
				continue;

			event.register(key.keybind);
		}
	}

	public KeyMapping getKeybind() {
		return keybind;
	}

	public boolean isPressed() {
		if (!modifiable)
			return isKeyDown(key);
		// Fabric port: vanilla gives each physical key to only one KeyMapping, so isDown() is false whenever another
		// mapping shares the key (toolmenu/toolbelt both default to Left Alt; the conflict-safe modifiers never get it,
		// see ConflictSafeKeyMappingMixin). NeoForge lets them share; the raw state of the bound key matches that.
		return isBoundKeyDown(keybind);
	}

	public String getBoundKey() {
		return keybind.getTranslatedKeyMessage()
			.getString()
			.toUpperCase();
	}

	public boolean doesModifierAndCodeMatch(int code) {
		// Fabric port: vanilla/Fabric key mappings have no NeoForge-style modifier (ctrl/alt/shift) component
		return code == keybind.getKey().getValue();
	}

	public static boolean isKeyDown(int key) {
		return InputConstants.isKeyDown(Minecraft.getInstance()
			.getWindow()
			.getWindow(), key);
	}

	public static boolean isMouseButtonDown(int button) {
		return GLFW.glfwGetMouseButton(Minecraft.getInstance()
			.getWindow()
			.getWindow(), button) == 1;
	}

	private static boolean isBoundKeyDown(KeyMapping mapping) {
		InputConstants.Key bound = mapping.getKey();
		return switch (bound.getType()) {
			case KEYSYM -> bound.getValue() != InputConstants.UNKNOWN.getValue() && isKeyDown(bound.getValue());
			case MOUSE -> isMouseButtonDown(bound.getValue());
			case SCANCODE -> false;
		};
	}

	public static boolean ctrlDown() {
		return isBoundKeyDown(CTRL_MODIFIER.keybind);
	}

	public static boolean shiftDown() {
		return isBoundKeyDown(SHIFT_MODIFIER.keybind);
	}

	public static boolean altDown() {
		return isBoundKeyDown(ALT_MODIFIER.keybind);
	}

}
