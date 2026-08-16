package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.material.FluidState;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(FluidState.class)
public abstract class FluidStateExtensionMixin implements com.simibubi.create.foundation.extension.FluidStateExtension {}
