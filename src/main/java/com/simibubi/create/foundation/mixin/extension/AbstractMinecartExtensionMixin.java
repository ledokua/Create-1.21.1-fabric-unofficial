package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.vehicle.AbstractMinecart;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartExtensionMixin implements com.simibubi.create.foundation.extension.AbstractMinecartExtension {}
