package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.mixin.extension.BlockEntityExtension;
import com.simibubi.create.foundation.model.data.ModelData;

import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Exposes {@link BlockEntityExtension#getModelData()} as Fabric render data so models can read it, and backs
 * {@link BlockEntityExtension#getPersistentData()} (saved under {@code NeoForgeData}, NeoForge's key, so
 * worlds moved between loaders keep the data).
 */
@Mixin(BlockEntity.class)
public class BlockEntityExtensionMixin implements BlockEntityExtension, RenderDataBlockEntity {

	@Unique
	private static final String CREATE$PERSISTENT_DATA_KEY = "NeoForgeData";

	@Unique
	private CompoundTag create$persistentData;

	@Override
	public Object getRenderData() {
		ModelData data = getModelData();
		return data == ModelData.EMPTY ? null : data;
	}

	@Override
	public CompoundTag getPersistentData() {
		if (create$persistentData == null)
			create$persistentData = new CompoundTag();
		return create$persistentData;
	}

	@Inject(method = "saveAdditional", at = @At("RETURN"))
	private void create$savePersistentData(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
		if (create$persistentData != null && !create$persistentData.isEmpty())
			tag.put(CREATE$PERSISTENT_DATA_KEY, create$persistentData.copy());
	}

	@Inject(method = "loadAdditional", at = @At("HEAD"))
	private void create$loadPersistentData(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
		if (tag.contains(CREATE$PERSISTENT_DATA_KEY, CompoundTag.TAG_COMPOUND))
			create$persistentData = tag.getCompound(CREATE$PERSISTENT_DATA_KEY);
	}
}
