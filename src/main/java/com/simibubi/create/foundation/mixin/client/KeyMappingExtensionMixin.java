package com.simibubi.create.foundation.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.foundation.mixin.extension.KeyMappingExtension;

import net.minecraft.client.KeyMapping;

@Mixin(KeyMapping.class)
public class KeyMappingExtensionMixin implements KeyMappingExtension {

	@Shadow
	private InputConstants.Key key;

	@Override
	public InputConstants.Key getKey() {
		return key;
	}
}
