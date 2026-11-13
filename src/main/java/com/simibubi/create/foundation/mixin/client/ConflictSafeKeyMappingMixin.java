package com.simibubi.create.foundation.mixin.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.client.ConflictSafeKeyMapping;
import net.minecraft.client.KeyMapping;

/**
 * Fabric port: vanilla routes each physical key to a single {@link KeyMapping} ({@code KeyMapping.MAP}). On NeoForge,
 * Catnip's {@link ConflictSafeKeyMapping} (Create's Shift/Ctrl/Alt modifiers) shares its key with other mappings; here
 * it would take the key from e.g. sneak or sprint when a player binds them to the same key.
 * <p>
 * So conflict-safe mappings never enter {@code MAP} (they get no press/click state and are read from the raw key state
 * instead, see {@code AllKeys}), and the Controls screen doesn't report them as conflicts. They stay in the options
 * list, so they can still be rebound.
 */
@Mixin(KeyMapping.class)
public class ConflictSafeKeyMappingMixin {

	@Shadow
	@Final
	private static Map<InputConstants.Key, KeyMapping> MAP;

	@WrapOperation(
		method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V",
		at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private Object create$skipKeyMapOnCreate(Map<Object, Object> map, Object key, Object mapping, Operation<Object> original) {
		return create$putUnlessConflictSafe(map, key, mapping, original);
	}

	@WrapOperation(
		method = "resetMapping",
		at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private static Object create$skipKeyMapOnReset(Map<Object, Object> map, Object key, Object mapping,
		Operation<Object> original) {
		return create$putUnlessConflictSafe(map, key, mapping, original);
	}

	private static Object create$putUnlessConflictSafe(Map<Object, Object> map, Object key, Object mapping,
		Operation<Object> original) {
		if (map == (Object) MAP && mapping instanceof ConflictSafeKeyMapping)
			return null;
		return original.call(map, key, mapping);
	}

	@Inject(method = "same", at = @At("HEAD"), cancellable = true)
	private void create$conflictSafeNeverConflicts(KeyMapping other, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ConflictSafeKeyMapping || other instanceof ConflictSafeKeyMapping)
			cir.setReturnValue(false);
	}
}
