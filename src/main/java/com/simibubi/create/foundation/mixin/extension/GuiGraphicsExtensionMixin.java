package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsExtensionMixin implements com.simibubi.create.foundation.extension.GuiGraphicsExtension {}
