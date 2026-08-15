package com.simibubi.create.foundation.mixin.extension;

import net.minecraft.nbt.CompoundTag;

/** Injected onto {@code net.minecraft.world.entity.Entity}; implemented by {@code EntityPersistentDataMixin}. */
public interface EntityExtension {

	/** Mod-private NBT that persists with the entity (NeoForge's {@code Entity#getPersistentData()}). */
	default CompoundTag getPersistentData() {
		throw new AbstractMethodError("EntityExtension#getPersistentData not implemented by mixin");
	}

	/**
	 * NeoForge's drop capture: while a non-null collection is set, item drops from {@code spawnAtLocation} are
	 * added to it instead of being spawned. Returns the previously set collection. Backed by
	 * {@code EntityPersistentDataMixin}.
	 */
	@org.jetbrains.annotations.Nullable
	default java.util.Collection<net.minecraft.world.entity.item.ItemEntity> captureDrops(@org.jetbrains.annotations.Nullable java.util.Collection<net.minecraft.world.entity.item.ItemEntity> value) {
		throw new AbstractMethodError("not implemented by mixin");
	}

	@org.jetbrains.annotations.Nullable
	default java.util.Collection<net.minecraft.world.entity.item.ItemEntity> captureDrops() {
		throw new AbstractMethodError("not implemented by mixin");
	}

	/** NeoForge's {@code Entity#getEyeInFluidType()}: the fluid type at eye level (EMPTY_TYPE if none). */
	default com.simibubi.create.foundation.transfer.fluid.FluidType getEyeInFluidType() {
		net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) this;
		net.minecraft.core.BlockPos eye = net.minecraft.core.BlockPos.containing(self.getX(), self.getEyeY(), self.getZ());
		net.minecraft.world.level.material.FluidState fluid = self.level().getFluidState(eye);
		double height = eye.getY() + fluid.getHeight(self.level(), eye);
		return height > self.getEyeY() ? com.simibubi.create.foundation.transfer.fluid.FluidTypeRegistry.get(fluid.getType())
			: com.simibubi.create.foundation.transfer.fluid.CommonFluids.EMPTY_TYPE.value();
	}

	default boolean isEyeInFluidType(com.simibubi.create.foundation.transfer.fluid.FluidType type) {
		return getEyeInFluidType() == type;
	}

	/** The item shown when middle-clicking this entity. */
	default net.minecraft.world.item.ItemStack getPickedResult(net.minecraft.world.phys.HitResult target) {
		net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) this;
		net.minecraft.world.item.ItemStack result = self.getPickResult();
		return result == null ? net.minecraft.world.item.ItemStack.EMPTY : result;
	}

	/** Called when the entity is removed from its level (both sides). Wired by an Entity mixin. */
	default void onRemovedFromLevel() {}

	default net.minecraft.world.entity.MobCategory getClassification(boolean forSpawnCount) {
		return ((net.minecraft.world.entity.Entity) this).getType().getCategory();
	}

	default boolean isAddedToLevel() {
		net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) this;
		return self.level() != null && self.level().getEntity(self.getId()) == self;
	}

	default boolean canDrownInFluidType(com.simibubi.create.foundation.transfer.fluid.FluidType type) {
		net.minecraft.world.entity.Entity self = (net.minecraft.world.entity.Entity) this;
		return self instanceof net.minecraft.world.entity.LivingEntity living && type.canDrownIn(living);
	}

	default net.minecraft.nbt.CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
		return ((net.minecraft.world.entity.Entity) this).saveWithoutId(new net.minecraft.nbt.CompoundTag());
	}

	default void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, net.minecraft.nbt.CompoundTag nbt) {
		((net.minecraft.world.entity.Entity) this).load(nbt);
	}
}
