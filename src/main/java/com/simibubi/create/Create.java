package com.simibubi.create;

import java.util.Random;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.registrate.CreateRegistrateRegistrationCallback;
import com.simibubi.create.compat.computercraft.ComputerCraftProxy;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileBlockHitActions;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileRenderModes;
import com.simibubi.create.content.fluids.tank.BoilerHeaters;
import com.simibubi.create.content.kinetics.TorquePropagator;
import com.simibubi.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.packagePort.AllPackagePortTargetTypes;
import com.simibubi.create.content.logistics.packager.AllInventoryIdentifiers;
import com.simibubi.create.content.logistics.packager.AllUnpackingHandlers;
import com.simibubi.create.content.logistics.packagerLink.GlobalLogisticsManager;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.schematics.ServerSchematicLoader;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.CreateNBTProcessors;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.simibubi.create.foundation.recipe.AllIngredients;
import com.simibubi.create.infrastructure.command.ServerLagger;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.worldgen.AllFeatures;
import com.simibubi.create.infrastructure.worldgen.AllPlacementModifiers;

import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.LangBuilder;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;

/**
 * Fabric entrypoint. Mirrors upstream NeoForge {@code Create} (its constructor + the FMLCommonSetupEvent /
 * RegisterEvent handlers) as one ordered {@link #onInitialize()}: NeoForge's mod-loading phases collapse
 * to "everything runs during onInitialize, in this order" on Fabric — see PORTING_PLAN.md, Phase 0b, for
 * how the Registrate flush ({@code REGISTRATE.register()}) takes the place of RegisterEvent.
 */
public class Create implements ModInitializer {
	public static final String ID = "create";
	public static final String NAME = "Create";

	public static final Logger LOGGER = LogUtils.getLogger();

	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/**
	 * Use the {@link Random} of a local {@link Level} or {@link Entity} or create one
	 */
	@Deprecated
	public static final Random RANDOM = new Random();

	/**
	 * <b>Other mods should not use this field!</b> If you are an addon developer, create your own instance of
	 * {@link CreateRegistrate}.
	 * </br
	 * If you were using this instance to register a callback listener use {@link CreateRegistrateRegistrationCallback#register} instead.
	 */
	private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
		.setTooltipModifierFactory(item ->
			new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
				.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
		);

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final GlobalLogisticsManager LOGISTICS = new GlobalLogisticsManager();
	public static final ServerLagger LAGGER = new ServerLagger();

	@Override
	public void onInitialize() {
		onCtor();
		// NeoForge: fired once per registry after the mod constructors ran. Here: the Registrate flush.
		REGISTRATE.register();
		onRegister();
		init();
	}

	/** Upstream's constructor body: file everything for registration. */
	public static void onCtor() {
		LOGGER.info("{} {} initializing! Commit hash: {}", NAME, CreateBuildInfo.VERSION, CreateBuildInfo.GIT_COMMIT);

		AllSoundEvents.prepare();
		AllCreativeModeTabs.register();
		AllArmorMaterials.register();
		AllDisplaySources.register();
		AllDisplayTargets.register();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllPaletteBlocks.register();
		AllMenuTypes.register();
		AllEntityTypes.register();
		AllBlockEntityTypes.register();
		AllRecipeTypes.register();
		AllParticleTypes.register();
		AllStructureProcessorTypes.register();
		AllEntityDataSerializers.register();
		AllPackets.register();
		AllFeatures.register();
		AllPlacementModifiers.register();
		AllIngredients.register();
		AllAttachmentTypes.register();
		AllDataComponents.register();
		AllMapDecorationTypes.register();
		AllMountedStorageTypes.register();

		AllConfigs.register();

		// TODO - Make these use Registry.register and move them into the RegisterEvent
		AllPackagePortTargetTypes.register();

		AllSchematicStateFilters.registerDefaults();

		// FIXME: some of these registrations are not thread-safe
		BogeySizes.init();
		AllBogeyStyles.init();
		// ----

		ComputerCraftProxy.register();

		AllSoundEvents.register();
	}

	/** Upstream's RegisterEvent handler (things that need to run once registries are populated). */
	public static void onRegister() {
		AllArmInteractionPointTypes.init();
		AllFanProcessingTypes.init();
		AllItemAttributeTypes.init();
		AllContraptionTypes.init();
		AllPotatoProjectileRenderModes.init();
		AllPotatoProjectileEntityHitActions.init();
		AllPotatoProjectileBlockHitActions.init();

		AllAdvancements.register();
		AllTriggers.register();
	}

	/** Upstream's FMLCommonSetupEvent handler. */
	public static void init() {
		AllFluids.registerFluidInteractions();
		CreateNBTProcessors.register();

		// TODO: custom registration should all happen in one place
		// Most registration happens in the constructor.
		// These registrations use Create's registered objects directly so they must run after registration has finished.
		BoilerHeaters.registerDefaults();
		AllPortalTracks.registerDefaults();
		AllBlockSpoutingBehaviours.registerDefaults();
		AllMovementBehaviours.registerDefaults();
		AllInteractionBehaviours.registerDefaults();
		AllContraptionMovementSettings.registerDefaults();
		AllOpenPipeEffectHandlers.registerDefaults();
		AllMountedDispenseItemBehaviors.registerDefaults();
		AllUnpackingHandlers.registerDefaults();
		AllInventoryIdentifiers.registerDefaults();
		// --
	}

	public static LangBuilder lang() {
		return new LangBuilder(ID);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static CreateRegistrate registrate() {
		if (!STACK_WALKER.getCallerClass().getPackageName().startsWith("com.simibubi.create"))
			throw new UnsupportedOperationException("Other mods are not permitted to use create's registrate instance.");
		return REGISTRATE;
	}
}
