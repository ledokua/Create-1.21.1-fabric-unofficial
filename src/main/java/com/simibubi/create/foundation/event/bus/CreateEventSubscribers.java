package com.simibubi.create.foundation.event.bus;

import java.util.List;

/**
 * Every class annotated {@code @EventBusSubscriber} in this codebase. Upstream finds these by classpath
 * scanning; we list them explicitly (as strings — client-only ones must not be loaded on a server). Keep in
 * sync when porting: {@code grep -rl "@EventBusSubscriber" src/main/java} must equal this list.
 */
public final class CreateEventSubscribers {

	private CreateEventSubscribers() {}

	public static final List<String> CLASSES = List.of(
		// GENERATED-BEGIN (scripts/list-event-subscribers.sh)
		"com.simibubi.create.AllKeys",
		"com.simibubi.create.compat.trainmap.TrainMapEvents",
		"com.simibubi.create.content.contraptions.ContraptionHandlerClient",
		"com.simibubi.create.content.contraptions.glue.SuperGlueHandler",
		"com.simibubi.create.content.contraptions.glue.SuperGlueItem",
		"com.simibubi.create.content.contraptions.minecart.CouplingHandler",
		"com.simibubi.create.content.contraptions.minecart.MinecartCouplingItem",
		"com.simibubi.create.content.contraptions.mounted.MinecartContraptionItem",
		"com.simibubi.create.content.equipment.armor.CardboardArmorHandler",
		"com.simibubi.create.content.equipment.armor.CardboardArmorHandlerClient",
		"com.simibubi.create.content.equipment.armor.DivingBootsItem",
		"com.simibubi.create.content.equipment.armor.DivingHelmetItem",
		"com.simibubi.create.content.equipment.armor.NetheriteBacktankFirstPersonRenderer",
		"com.simibubi.create.content.equipment.armor.NetheriteDivingHandler",
		"com.simibubi.create.content.equipment.bell.HauntedBellPulser",
		"com.simibubi.create.content.equipment.clipboard.ClipboardValueSettingsHandler",
		"com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem",
		"com.simibubi.create.content.equipment.extendoGrip.ExtendoGripRenderHandler",
		"com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler",
		"com.simibubi.create.content.equipment.tool.CardboardSwordItem",
		"com.simibubi.create.content.equipment.wrench.WrenchEventHandler",
		"com.simibubi.create.content.equipment.zapper.ZapperInteractionHandler",
		"com.simibubi.create.content.fluids.FluidBottleItemHook",
		"com.simibubi.create.content.fluids.FluidReactions",
		"com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorConnectionHandler",
		"com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorInteractionHandler",
		"com.simibubi.create.content.kinetics.crank.ValveHandleBlock",
		"com.simibubi.create.content.kinetics.crusher.CrushingWheelBlockEntity",
		"com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer",
		"com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe",
		"com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointHandler",
		"com.simibubi.create.content.logistics.box.PackageClientInteractionHandler",
		"com.simibubi.create.content.logistics.depot.EjectorTargetHandler",
		"com.simibubi.create.content.logistics.funnel.FunnelItem",
		"com.simibubi.create.content.logistics.itemHatch.ItemHatchHandler",
		"com.simibubi.create.content.logistics.stockTicker.StockTickerInteractionHandler",
		"com.simibubi.create.content.processing.burner.BlazeBurnerHandler",
		"com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem",
		"com.simibubi.create.content.redstone.link.LinkHandler",
		"com.simibubi.create.content.trains.schedule.ScheduleItemEntityInteraction",
		"com.simibubi.create.content.trains.track.TrackBlockItem",
		"com.simibubi.create.content.trains.track.TrackBlockOutline",
		"com.simibubi.create.foundation.block.ItemUseOverrides",
		"com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler",
		"com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionHandler",
		"com.simibubi.create.foundation.events.ClientEvents",
		"com.simibubi.create.foundation.events.CommonEvents",
		"com.simibubi.create.foundation.events.CommonEvents$ModBusEvents",
		"com.simibubi.create.foundation.events.InputEvents",
		"com.simibubi.create.foundation.render.RenderTypes$Shaders",
		"com.simibubi.create.impl.registry.CreateRegistriesImpl",
		"com.simibubi.create.infrastructure.RemapHelper",
		"com.simibubi.create.infrastructure.config.AllConfigs",
		"com.simibubi.create.infrastructure.gametest.CreateGameTests",
		"com.simibubi.create.infrastructure.gui.OpenCreateMenuButton$OpenConfigButtonHandler"
		// GENERATED-END
	);
}
