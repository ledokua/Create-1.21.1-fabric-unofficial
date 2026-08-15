package com.simibubi.create.foundation.mixin.extension;

/** Injected onto {@code AbstractMinecart}: NeoForge's slope-adjustment accessor (vanilla's constant). */
public interface AbstractMinecartExtension {
	default double getSlopeAdjustment() {
		return 0.0078125D;
	}

	private net.minecraft.world.entity.vehicle.AbstractMinecart cart() {
		return (net.minecraft.world.entity.vehicle.AbstractMinecart) this;
	}

	default boolean canUseRail() {
		return true;
	}

	default float getMaxCartSpeedOnRail() {
		return 1.2f;
	}

	/** Vanilla's max speed with rail = {@code getMaxSpeed()} (protected); same value the vanilla movement code uses. */
	default double getMaxSpeedWithRail() {
		return cart().isInWater() ? 0.2D : 0.4D;
	}

	default net.minecraft.core.BlockPos getCurrentRailPosition() {
		net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(cart().getX(), cart().getY(), cart().getZ());
		if (cart().level().getBlockState(pos.below()).is(net.minecraft.tags.BlockTags.RAILS))
			pos = pos.below();
		return pos;
	}

	default boolean canBeRidden() {
		return cart().getMinecartType() == net.minecraft.world.entity.vehicle.AbstractMinecart.Type.RIDEABLE;
	}

	/** Vanilla's protected {@code moveAlongTrack}; access-widened. */
	default void moveMinecartOnRail(net.minecraft.core.BlockPos pos) {
		cart().moveAlongTrack(pos, cart().level().getBlockState(pos));
	}
}
