package com.simibubi.create;

import java.util.Random;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;

/**
 * Fabric entrypoint mirroring upstream NeoForge {@code com.simibubi.create.Create}.
 *
 * This is intentionally a bare skeleton right now (see PORTING_PLAN.md, Phase 0). Upstream's constructor
 * wires up {@code CreateRegistrate} and calls into ~25 {@code AllXxx.register(...)} classes; none of that
 * can be ported until Phase 0b (a Fabric-native replacement for Registrate) lands, since {@code
 * CreateRegistrate extends AbstractRegistrate} from that library. Do not add ad-hoc registration calls here
 * before then — port them through the registration layer once it exists, the same way upstream does.
 */
public class Create implements ModInitializer {
	public static final String ID = "create";
	public static final String NAME = "Create";

	public static final Logger LOGGER = LogUtils.getLogger();

	/**
	 * Use the {@link Random} of a local level or entity, or create one, instead where possible.
	 */
	@Deprecated
	public static final Random RANDOM = new Random();

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	// TODO(Phase 0b): public static final CreateRegistrate REGISTRATE = ...
	// TODO(Phase 1): SCHEMATIC_RECEIVER, REDSTONE_LINK_NETWORK_HANDLER, TORQUE_PROPAGATOR, RAILWAYS,
	//                LOGISTICS, LAGGER — port alongside their owning subsystems, not ahead of them.

	@Override
	public void onInitialize() {
		LOGGER.info("{} initializing (Fabric port, {})", NAME, ID);

		// TODO(Phase 0b+): mirror Create#onCtor's registration sequence once CreateRegistrate exists.
	}
}
