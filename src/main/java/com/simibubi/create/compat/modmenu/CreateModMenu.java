package com.simibubi.create.compat.modmenu;

import com.simibubi.create.Create;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Mod Menu entrypoint: opens Create's config screen (upstream registers it as NeoForge's IConfigScreenFactory). */
@Environment(EnvType.CLIENT)
public class CreateModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new BaseConfigScreen(parent, Create.ID);
	}
}
