package com.simibubi.create.foundation.mixin.event;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.entity.living.LivingBreatheEvent;
import com.simibubi.create.foundation.event.entity.living.LivingDropsEvent;
import com.simibubi.create.foundation.event.entity.living.LivingEvent;
import com.simibubi.create.foundation.event.entity.living.LivingExperienceDropEvent;
import com.simibubi.create.foundation.event.entity.living.LivingIncomingDamageEvent;
import com.simibubi.create.foundation.event.entity.living.LivingKnockBackEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

/** The NeoForge living-entity events Create listens to: knockback, incoming damage, xp/item drops, visibility, breathing. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEventsMixin implements DropCapture {
	@Shadow
	protected int lastHurtByPlayerTime;

	@Shadow
	@Nullable
	protected Player lastHurtByPlayer;

	@Shadow
	public abstract boolean canBreatheUnderwater();

	@Unique
	@Nullable
	private List<ItemEntity> create$capturedDrops;

	private LivingEntity create$self() {
		return (LivingEntity) (Object) this;
	}

	// ---- LivingKnockBackEvent ----
	@ModifyVariable(method = "knockback", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private double create$knockback(double strength, double strengthArg, double xRatio, double zRatio) {
		LivingKnockBackEvent event = CreateEventBus.BUS.post(new LivingKnockBackEvent(create$self(), (float) strength, xRatio, zRatio));
		return event.isCanceled() ? 0 : event.getStrength();
	}

	// ---- LivingIncomingDamageEvent (server side, like NeoForge) ----
	@WrapMethod(method = "hurt")
	private boolean create$hurt(DamageSource source, float amount, Operation<Boolean> original) {
		LivingEntity self = create$self();
		if (self.level().isClientSide || self.isInvulnerableTo(source))
			return original.call(source, amount);
		LivingIncomingDamageEvent event = CreateEventBus.BUS.post(new LivingIncomingDamageEvent(self, source, amount));
		if (event.isCanceled())
			return false;
		return original.call(source, event.getAmount());
	}

	// ---- LivingExperienceDropEvent ----
	@ModifyExpressionValue(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getExperienceReward(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I"))
	private int create$experience(int original) {
		LivingExperienceDropEvent event = CreateEventBus.BUS.post(new LivingExperienceDropEvent(create$self(), lastHurtByPlayer, original));
		return event.isCanceled() ? 0 : event.getDroppedExperience();
	}

	// ---- LivingVisibilityEvent ----
	@ModifyReturnValue(method = "getVisibilityPercent", at = @At("RETURN"))
	private double create$visibility(double original, Entity lookingEntity) {
		LivingEvent.LivingVisibilityEvent event = CreateEventBus.BUS.post(new LivingEvent.LivingVisibilityEvent(create$self(), lookingEntity, original));
		return Math.max(0, event.getVisibilityModifier());
	}

	// ---- LivingBreatheEvent: forces vanilla into the "can't breathe" or "refill" branch ----
	@ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
	private boolean create$breathe(boolean eyeInWater) {
		LivingEntity self = create$self();
		boolean invulnerable = self instanceof Player player && player.getAbilities().invulnerable;
		boolean canBreathe = !eyeInWater || canBreatheUnderwater() || MobEffectUtil.hasWaterBreathing(self) || invulnerable;
		LivingBreatheEvent event = CreateEventBus.BUS.post(new LivingBreatheEvent(self, canBreathe, 1, 4, true));
		if (event.canBreathe())
			return false; // vanilla: refill air
		return eyeInWater; // vanilla decides via its own checks (matches NeoForge unless a listener forced canBreathe=false)
	}

	// ---- LivingDropsEvent: capture drops during dropAllDeathLoot, post, then spawn ----
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void create$startCapture(ServerLevel level, DamageSource source, CallbackInfo ci) {
		create$capturedDrops = new ArrayList<>();
	}

	@Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
	private void create$endCapture(ServerLevel level, DamageSource source, CallbackInfo ci) {
		List<ItemEntity> drops = create$capturedDrops;
		create$capturedDrops = null;
		if (drops == null)
			return;
		LivingDropsEvent event = CreateEventBus.BUS.post(new LivingDropsEvent(create$self(), source, drops, lastHurtByPlayerTime > 0));
		if (event.isCanceled())
			return;
		for (ItemEntity drop : event.getDrops())
			level.addFreshEntity(drop);
	}

	@Override
	public boolean create$captureDrop(ItemEntity itemEntity) {
		if (create$capturedDrops == null)
			return false;
		create$capturedDrops.add(itemEntity);
		return true;
	}
}
