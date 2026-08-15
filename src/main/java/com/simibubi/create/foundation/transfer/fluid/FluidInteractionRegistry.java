package com.simibubi.create.foundation.transfer.fluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * "When fluid A flows next to fluid B, place block C" (lava + water → obsidian/cobble, and Create's lava +
 * honey → limestone etc.). Create-owned (was NeoForge's, which patches LiquidBlock to consult it); here a
 * mixin on {@code LiquidBlock#shouldSpreadLiquid} calls {@link #canInteract}. Vanilla's own lava/water
 * handling stays untouched — only interactions registered here are added on top.
 */
public final class FluidInteractionRegistry {

	private static final Map<FluidType, List<InteractionInformation>> INTERACTIONS = new HashMap<>();

	private FluidInteractionRegistry() {}

	public static synchronized void addInteraction(FluidType source, InteractionInformation interaction) {
		INTERACTIONS.computeIfAbsent(source, s -> new ArrayList<>()).add(interaction);
	}

	/** @return true if an interaction fired at {@code pos} (the fluid at {@code pos} being the source type). */
	public static boolean canInteract(Level level, BlockPos pos) {
		FluidState state = level.getFluidState(pos);
		List<InteractionInformation> interactions = INTERACTIONS.getOrDefault(FluidTypeRegistry.get(state.getType()), Collections.emptyList());
		if (interactions.isEmpty())
			return false;
		for (Direction direction : Direction.values()) {
			BlockPos relativePos = pos.relative(direction.getOpposite());
			for (InteractionInformation interaction : interactions) {
				if (interaction.predicate().test(level, pos, relativePos, state)) {
					interaction.interaction().interact(level, pos, relativePos, state);
					return true;
				}
			}
		}
		return false;
	}

	public static Map<FluidType, List<InteractionInformation>> getInteractions() {
		return Collections.unmodifiableMap(INTERACTIONS);
	}

	public record InteractionInformation(HasFluidInteraction predicate, FluidInteraction interaction) {

		public InteractionInformation(FluidType type, BlockState state) {
			this(type, fluidState -> state);
		}

		public InteractionInformation(HasFluidInteraction predicate, BlockState state) {
			this(predicate, fluidState -> state);
		}

		public InteractionInformation(FluidType type, Function<FluidState, BlockState> getState) {
			this((level, currentPos, relativePos, currentState) -> FluidTypeRegistry.get(level.getFluidState(relativePos).getType()) == type, getState);
		}

		public InteractionInformation(HasFluidInteraction predicate, Function<FluidState, BlockState> getState) {
			this(predicate, (level, currentPos, relativePos, currentState) -> {
				level.setBlockAndUpdate(currentPos, getState.apply(currentState));
				level.levelEvent(1501, currentPos, 0);
			});
		}
	}

	@FunctionalInterface
	public interface HasFluidInteraction {
		boolean test(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
	}

	@FunctionalInterface
	public interface FluidInteraction {
		void interact(Level level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
	}
}
