package com.simibubi.create.foundation.mixin.compat.journeymap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.Create;
import com.simibubi.create.compat.trainmap.JourneyTrainMap;

import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws the train map overlay on JourneyMap's fullscreen map, right after the map itself is drawn. Upstream (NeoForge)
 * uses JourneyMap's {@code @JourneyMapPlugin} API instead; a mixin avoids depending on JourneyMap's plugin scanner.
 */
@Mixin(Fullscreen.class)
public abstract class JourneyFullscreenMapMixin {
	@Unique
	private boolean create$failedToRenderTrainMap = false;

	// Member reference without a descriptor: the descriptor would contain Minecraft types, which must not be
	// remapped here (the owner is JourneyMap's own class), and drawMap has no overloads.
	@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE",
		target = "Ljourneymap/client/ui/fullscreen/Fullscreen;drawMap", shift = Shift.AFTER, remap = false),
		require = 0)
	private void create$journeyMapFullscreenRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
		CallbackInfo ci) {
		try {
			if (create$failedToRenderTrainMap)
				return;
			Fullscreen self = (Fullscreen) (Object) this;
			JourneyTrainMap.onRender(graphics, self, self.getCenterBlockX(true), self.getCenterBlockZ(true), mouseX,
				mouseY, partialTicks);
		} catch (Throwable e) {
			Create.LOGGER.error("Failed to render JourneyMap train map integration:", e);
			create$failedToRenderTrainMap = true;
		}
	}
}
