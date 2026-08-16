package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(ChunkAccess.class)
public abstract class ChunkAccessExtensionMixin implements com.simibubi.create.foundation.attachment.IAttachmentHolder, com.simibubi.create.foundation.extension.ChunkAccessExtension {}
