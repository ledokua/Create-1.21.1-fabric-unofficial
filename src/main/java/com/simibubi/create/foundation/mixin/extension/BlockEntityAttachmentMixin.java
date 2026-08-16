package com.simibubi.create.foundation.mixin.extension;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 */
@Mixin(BlockEntity.class)
public abstract class BlockEntityAttachmentMixin implements com.simibubi.create.foundation.attachment.IAttachmentHolder {}
