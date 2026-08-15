package com.simibubi.create.foundation.mixin.event.client;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.event.bus.CreateEventBus;
import com.simibubi.create.foundation.event.client.ModelEvent;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;

/** NeoForge's {@code ModelEvent.ModifyBakingResult}: lets Create's ModelSwapper replace top-level baked models. */
@Mixin(ModelBakery.class)
public class ModelBakeryEventsMixin {
	@Shadow
	@Final
	private Map<ModelResourceLocation, BakedModel> bakedTopLevelModels;

	@Inject(method = "bakeModels", at = @At("TAIL"))
	private void create$modifyBakingResult(ModelBakery.TextureGetter textureGetter, CallbackInfo ci) {
		CreateEventBus.BUS.post(new ModelEvent.ModifyBakingResult(bakedTopLevelModels));
	}
}
