package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.DyeColor;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(DyeColor.class)
public abstract class DyeColorExtensionMixin implements com.simibubi.create.foundation.extension.DyeColorExtension {}
