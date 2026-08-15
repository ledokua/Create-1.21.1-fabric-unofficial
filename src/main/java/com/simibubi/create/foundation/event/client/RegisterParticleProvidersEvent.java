package com.simibubi.create.foundation.event.client;

import com.simibubi.create.foundation.event.bus.Event;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

/** Lifecycle event: register particle providers. Backed by Fabric's ParticleFactoryRegistry. */
public class RegisterParticleProvidersEvent extends Event {

	public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type, ParticleProvider<T> provider) {
		ParticleFactoryRegistry.getInstance().register(type, provider);
	}

	public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> registration) {
		ParticleFactoryRegistry.getInstance().register(type, sprites -> registration.create(sprites));
	}
}
