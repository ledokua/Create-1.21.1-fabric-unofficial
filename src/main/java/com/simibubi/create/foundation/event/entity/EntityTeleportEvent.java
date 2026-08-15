package com.simibubi.create.foundation.event.entity;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Fired for teleports (ender pearl, chorus fruit, /tp, ...); handlers may adjust the target or cancel. */
public class EntityTeleportEvent extends EntityEvent implements ICancellableEvent {

	protected double targetX;
	protected double targetY;
	protected double targetZ;

	public EntityTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
		super(entity);
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
	}

	public double getTargetX() {
		return targetX;
	}

	public void setTargetX(double targetX) {
		this.targetX = targetX;
	}

	public double getTargetY() {
		return targetY;
	}

	public void setTargetY(double targetY) {
		this.targetY = targetY;
	}

	public double getTargetZ() {
		return targetZ;
	}

	public void setTargetZ(double targetZ) {
		this.targetZ = targetZ;
	}

	public Vec3 getTarget() {
		return new Vec3(targetX, targetY, targetZ);
	}

	public double getPrevX() {
		return getEntity().getX();
	}

	public double getPrevY() {
		return getEntity().getY();
	}

	public double getPrevZ() {
		return getEntity().getZ();
	}

	public Vec3 getPrev() {
		return getEntity().position();
	}

	public static class ChorusFruit extends EntityTeleportEvent {
		public ChorusFruit(net.minecraft.world.entity.LivingEntity entity, double targetX, double targetY, double targetZ) {
			super(entity, targetX, targetY, targetZ);
		}

		@Override
		public net.minecraft.world.entity.LivingEntity getEntity() {
			return (net.minecraft.world.entity.LivingEntity) super.getEntity();
		}
	}
}
