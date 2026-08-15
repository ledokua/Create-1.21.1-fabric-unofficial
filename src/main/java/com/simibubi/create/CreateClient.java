package com.simibubi.create;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entrypoint mirroring upstream NeoForge {@code com.simibubi.create.CreateClient}.
 *
 * Bare skeleton for now — see PORTING_PLAN.md, Phase 0. Upstream's client setup wires up
 * ModelSwapper/CasingConnectivity/schematic handlers/Ponder/config screens, all of which depend on content
 * or infra that hasn't been ported yet. Port those alongside their owning subsystems.
 */
public class CreateClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		Create.LOGGER.info("{} client initializing", Create.NAME);
	}
}
