package com.simibubi.create.foundation.mixin.hooks;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.simibubi.create.foundation.extension.LightEngineLevelAccess;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LightEngine;

@Mixin(LightEngine.class)
public abstract class LightEngineLevelMixin implements LightEngineLevelAccess {
	@Shadow
	@Final
	protected LightChunkGetter chunkSource;

	@Override
	public BlockGetter create$level() {
		return chunkSource.getLevel();
	}
}
