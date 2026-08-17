package com.simibubi.create.foundation.mixin.compat.xaeros;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.Create;
import com.simibubi.create.compat.trainmap.XaeroTrainMap;

import net.minecraft.client.gui.GuiGraphics;
import xaero.map.gui.GuiMap;

/**
 * Draws the train map overlay on Xaero's fullscreen world map. Upstream (NeoForge) injects into a blit call inside
 * GuiMap#render; on the Fabric builds of Xaero's World Map that call doesn't exist, so this hooks
 * {@code renderPreDropdown} instead — called by xaerolib's ScreenBase#render after the map itself has been drawn and
 * before Xaero's own dropdown UI, which is exactly where the overlay belongs.
 */
@Mixin(value = GuiMap.class, remap = false)
public abstract class XaeroFullscreenMapMixin {
	@Unique
	private boolean create$failedToRenderTrainMap = false;

	@Inject(method = "renderPreDropdown", at = @At("HEAD"), require = 0, remap = false)
	private void create$xaeroMapFullscreenRender(GuiGraphics guiGraphics, int scaledMouseX, int scaledMouseY,
		float partialTicks, CallbackInfo ci) {
		try {
			if (!create$failedToRenderTrainMap)
				XaeroTrainMap.onRender(guiGraphics, (GuiMap) (Object) this, scaledMouseX, scaledMouseY, partialTicks);
		} catch (Throwable e) {
			Create.LOGGER.error("Failed to render Xaero's World Map train map integration:", e);
			create$failedToRenderTrainMap = true;
		}
	}
}
