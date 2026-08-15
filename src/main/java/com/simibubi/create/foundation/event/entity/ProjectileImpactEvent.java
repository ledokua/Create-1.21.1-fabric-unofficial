package com.simibubi.create.foundation.event.entity;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

/** Fired when a projectile hits something; cancelling skips the impact. */
public class ProjectileImpactEvent extends EntityEvent implements ICancellableEvent {

	private final HitResult ray;
	private final Projectile projectile;

	public ProjectileImpactEvent(Projectile projectile, HitResult ray) {
		super(projectile);
		this.ray = ray;
		this.projectile = projectile;
	}

	public HitResult getRayTraceResult() {
		return ray;
	}

	public Projectile getProjectile() {
		return projectile;
	}
}
