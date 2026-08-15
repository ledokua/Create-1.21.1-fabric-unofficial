package com.simibubi.create.foundation.mixin.event;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.EntityEvent;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

/** NeoForge's {@code EntityEvent.EnteringSection} (server side; the client counterpart is in the client mixin set). */
@Mixin(PersistentEntitySectionManager.Callback.class)
public class EntitySectionChangeMixin<T extends EntityAccess> {
	@Shadow
	@Final
	private T entity;

	@Shadow
	private long currentSectionKey;

	@Inject(method = "onMove", at = @At("HEAD"))
	private void create$onMove(CallbackInfo ci) {
		if (!(entity instanceof Entity e))
			return;
		long newKey = SectionPos.asLong(e.blockPosition());
		if (newKey != currentSectionKey)
			CreateEventBus.BUS.post(new EntityEvent.EnteringSection(e, currentSectionKey, newKey));
	}
}
