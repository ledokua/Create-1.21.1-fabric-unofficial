package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.Block;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(Block.class)
public abstract class BlockExtensionMixin implements com.simibubi.create.foundation.extension.BlockExtension, com.simibubi.create.foundation.extension.IBlockExtension {}
