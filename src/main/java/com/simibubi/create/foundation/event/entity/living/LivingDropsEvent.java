package com.simibubi.create.foundation.event.entity.living;

import java.util.Collection;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

/** Fired when a living entity dies with drops computed; handlers may modify the drop list or cancel. */
public class LivingDropsEvent extends LivingEvent implements ICancellableEvent {

	private final DamageSource source;
	private final Collection<ItemEntity> drops;
	private final boolean recentlyHit;

	public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, boolean recentlyHit) {
		super(entity);
		this.source = source;
		this.drops = drops;
		this.recentlyHit = recentlyHit;
	}

	public DamageSource getSource() {
		return source;
	}

	public Collection<ItemEntity> getDrops() {
		return drops;
	}

	public boolean isRecentlyHit() {
		return recentlyHit;
	}
}
