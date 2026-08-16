package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(BlockEntityRenderer.class)
public interface BlockEntityRendererExtensionMixin<T extends net.minecraft.world.level.block.entity.BlockEntity> extends com.simibubi.create.foundation.extension.BlockEntityRendererExtension<T> {}
