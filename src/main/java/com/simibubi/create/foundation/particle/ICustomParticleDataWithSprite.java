package com.simibubi.create.foundation.particle;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import org.jetbrains.annotations.NotNull;

public interface ICustomParticleDataWithSprite<T extends ParticleOptions> extends ICustomParticleData<T> {

	default ParticleType<T> createType() {
		return new ParticleType<>(false) {

			@Override
			public @NotNull MapCodec<T> codec() {
				return ICustomParticleDataWithSprite.this.getCodec(this);
			}

			@Override
			public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
				return ICustomParticleDataWithSprite.this.getStreamCodec();
			}
		};
	}

	@Override
	@Environment(EnvType.CLIENT)
	default ParticleProvider<T> getFactory() {
		throw new IllegalAccessError("This particle type uses a metaFactory!");
	}

	@Environment(EnvType.CLIENT)
	public SpriteParticleRegistration<T> getMetaFactory();

	@Override
	@Environment(EnvType.CLIENT)
	public default void register(ParticleType<T> type, RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(type, getMetaFactory());
	}

}
