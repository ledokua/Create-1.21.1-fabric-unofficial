package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.foundation.extension.EntityExtension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

/**
 * Backs {@link EntityExtension#getPersistentData()}: a per-entity CompoundTag saved under
 * {@code NeoForgeData} — the same key NeoForge uses, so worlds moved between loaders keep the data.
 */
@Mixin(Entity.class)
public class EntityPersistentDataMixin implements EntityExtension, com.simibubi.create.foundation.extension.EntityExtension.PersistentDataHolder {

	@Unique
	private static final String CREATE$PERSISTENT_DATA_KEY = "NeoForgeData";

	@Unique
	private CompoundTag create$persistentData;

	// Deliberately NOT named getPersistentData(): several Fabric mods add an Entity#getPersistentData() of their own via
	// mixin, and two mixins adding the same method make Mixin skip one of them. The interface default routes here.
	@Override
	public CompoundTag create$persistentData() {
		if (create$persistentData == null)
			create$persistentData = new CompoundTag();
		return create$persistentData;
	}

	@Unique
	private java.util.Collection<net.minecraft.world.entity.item.ItemEntity> create$capturedDrops;

	@Override
	public java.util.Collection<net.minecraft.world.entity.item.ItemEntity> captureDrops(java.util.Collection<net.minecraft.world.entity.item.ItemEntity> value) {
		java.util.Collection<net.minecraft.world.entity.item.ItemEntity> previous = create$capturedDrops;
		create$capturedDrops = value;
		return previous;
	}

	@Override
	public java.util.Collection<net.minecraft.world.entity.item.ItemEntity> captureDrops() {
		return create$capturedDrops;
	}

	@Inject(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("HEAD"), cancellable = true)
	private void create$captureDrop(net.minecraft.world.item.ItemStack stack, float offsetY, CallbackInfoReturnable<net.minecraft.world.entity.item.ItemEntity> cir) {
		if (create$capturedDrops == null || stack.isEmpty())
			return;
		net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) (Object) this;
		if (self.level().isClientSide)
			return;
		net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(self.level(), self.getX(), self.getY() + offsetY, self.getZ(), stack);
		item.setDefaultPickUpDelay();
		create$capturedDrops.add(item);
		cir.setReturnValue(item);
	}

	@Inject(method = "saveWithoutId", at = @At("RETURN"))
	private void create$savePersistentData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
		if (create$persistentData != null && !create$persistentData.isEmpty())
			tag.put(CREATE$PERSISTENT_DATA_KEY, create$persistentData.copy());
	}

	@Inject(method = "load", at = @At("HEAD"))
	private void create$loadPersistentData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains(CREATE$PERSISTENT_DATA_KEY, CompoundTag.TAG_COMPOUND))
			create$persistentData = tag.getCompound(CREATE$PERSISTENT_DATA_KEY);
	}
}
