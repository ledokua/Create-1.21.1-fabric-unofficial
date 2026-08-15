package com.simibubi.create.foundation.event.client;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;

/** Fired when the client is about to play a sound; handlers may replace or (by nulling) suppress it. From a SoundEngine mixin. */
public class PlaySoundEvent extends Event {

	private final SoundEngine engine;
	private final SoundInstance originalSound;
	@Nullable
	private SoundInstance sound;

	public PlaySoundEvent(SoundEngine engine, SoundInstance sound) {
		this.engine = engine;
		this.originalSound = sound;
		this.sound = sound;
	}

	public SoundEngine getEngine() {
		return engine;
	}

	public SoundInstance getOriginalSound() {
		return originalSound;
	}

	@Nullable
	public SoundInstance getSound() {
		return sound;
	}

	public void setSound(@Nullable SoundInstance sound) {
		this.sound = sound;
	}

	public String getName() {
		return originalSound.getLocation().getPath();
	}
}
