package com.simibubi.create.foundation.event.client;

import com.simibubi.create.foundation.event.bus.Event;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

/** Lifecycle event: register key mappings. Backed by Fabric's KeyBindingHelper. */
public class RegisterKeyMappingsEvent extends Event {

	public void register(KeyMapping key) {
		KeyBindingHelper.registerKeyBinding(key);
	}
}
