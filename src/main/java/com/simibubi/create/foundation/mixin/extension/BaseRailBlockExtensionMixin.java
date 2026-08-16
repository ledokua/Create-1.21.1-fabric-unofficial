package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.BaseRailBlock;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(BaseRailBlock.class)
public abstract class BaseRailBlockExtensionMixin implements com.simibubi.create.foundation.extension.BaseRailBlockExtension {}
