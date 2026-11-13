package com.simibubi.create.compat.opac;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixins;

import com.mojang.logging.LogUtils;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/**
 * Fabric port: Open Parties and Claims ships its Create protection mixins (contraptions moving and placing blocks,
 * drills, harvesters, ploughs, deployers, mechanical arms, the schematicannon, the symmetry wand, ... respecting claims)
 * in its Fabric jar too, but registers them only on NeoForge, as no Fabric Create existed for 1.21. They target upstream
 * Create's classes, which this port keeps, so they are registered here: before any game class loads, but after Mixin
 * has read the configs listed in mod jars (adding one while it does that breaks it). OPAC's NeoForge-only part (claim
 * checks on Create's packets, open pipes, the symmetry wand's breaks) is redone in this port's mixin/compat/opac.
 * <p>
 * {@code -Dcreate.opacCompat=false} (or the environment variable {@code CREATE_OPAC_COMPAT=false}) skips it.
 */
public class OpacCreateSupport implements PreLaunchEntrypoint {
	private static final Logger LOGGER = LogUtils.getLogger();

	/** OPAC is installed and the compat isn't turned off; also gates this port's own OPAC mixins (mixin/compat/opac). */
	public static boolean enabled() {
		return FabricLoader.getInstance().isModLoaded("openpartiesandclaims")
			&& !"false".equals(System.getProperty("create.opacCompat")) && !"false".equals(System.getenv("CREATE_OPAC_COMPAT"));
	}

	@Override
	public void onPreLaunch() {
		if (!FabricLoader.getInstance().isModLoaded("openpartiesandclaims"))
			return;
		if (!enabled()) {
			LOGGER.info("Open Parties and Claims' Create protection turned off (create.opacCompat=false)");
			return;
		}
		Mixins.addConfiguration("openpartiesandclaims.create.mixins.json");
		LOGGER.info("Registered Open Parties and Claims' Create protection mixins");
	}
}
