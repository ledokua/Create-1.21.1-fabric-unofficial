package com.simibubi.create.foundation.platform;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

/** {@link SoundType} whose sounds are supplied lazily (they're registered after blocks are constructed). */
public class DeferredSoundType extends SoundType {

	private final Supplier<SoundEvent> breakSound;
	private final Supplier<SoundEvent> stepSound;
	private final Supplier<SoundEvent> placeSound;
	private final Supplier<SoundEvent> hitSound;
	private final Supplier<SoundEvent> fallSound;

	public DeferredSoundType(float volume, float pitch, Supplier<SoundEvent> breakSound, Supplier<SoundEvent> stepSound,
		Supplier<SoundEvent> placeSound, Supplier<SoundEvent> hitSound, Supplier<SoundEvent> fallSound) {
		super(volume, pitch, null, null, null, null, null);
		this.breakSound = breakSound;
		this.stepSound = stepSound;
		this.placeSound = placeSound;
		this.hitSound = hitSound;
		this.fallSound = fallSound;
	}

	@Override
	public SoundEvent getBreakSound() {
		return breakSound.get();
	}

	@Override
	public SoundEvent getStepSound() {
		return stepSound.get();
	}

	@Override
	public SoundEvent getPlaceSound() {
		return placeSound.get();
	}

	@Override
	public SoundEvent getHitSound() {
		return hitSound.get();
	}

	@Override
	public SoundEvent getFallSound() {
		return fallSound.get();
	}
}
