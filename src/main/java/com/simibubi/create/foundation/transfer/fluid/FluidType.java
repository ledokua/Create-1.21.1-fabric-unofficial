package com.simibubi.create.foundation.transfer.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.client.extensions.IClientFluidTypeExtensions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * Per-fluid properties bundle (density, viscosity, temperature, sounds, name, client appearance) — the
 * NeoForge concept Create's fluid code is written against, Create-owned. On Fabric the same information is
 * published to the ecosystem through {@code FluidVariantAttributes} (name/temperature/viscosity/luminance/
 * sounds) and {@code FluidRenderHandlerRegistry} (textures/tint) by {@link FluidTypeRegistry} when a
 * {@link BaseFlowingFluid} is registered. See PORTING_PLAN.md, "NeoForge → Fabric translation strategy".
 */
public class FluidType {

	public static final int BUCKET_VOLUME = 1000;

	private final String descriptionId;
	private final double motionScale;
	private final boolean canPushEntity;
	private final boolean canSwim;
	private final boolean canDrown;
	private final float fallDistanceModifier;
	private final boolean canExtinguish;
	private final boolean canConvertToSource;
	private final boolean supportsBoating;
	@Nullable
	private final PathType pathType;
	@Nullable
	private final PathType adjacentPathType;
	private final Map<SoundAction, SoundEvent> sounds;
	private final boolean canHydrate;
	private final int lightLevel;
	private final int density;
	private final int temperature;
	private final int viscosity;
	private final Rarity rarity;

	public FluidType(Properties properties) {
		this.descriptionId = properties.descriptionId;
		this.motionScale = properties.motionScale;
		this.canPushEntity = properties.canPushEntity;
		this.canSwim = properties.canSwim;
		this.canDrown = properties.canDrown;
		this.fallDistanceModifier = properties.fallDistanceModifier;
		this.canExtinguish = properties.canExtinguish;
		this.canConvertToSource = properties.canConvertToSource;
		this.supportsBoating = properties.supportsBoating;
		this.pathType = properties.pathType;
		this.adjacentPathType = properties.adjacentPathType;
		this.sounds = Map.copyOf(properties.sounds);
		this.canHydrate = properties.canHydrate;
		this.lightLevel = properties.lightLevel;
		this.density = properties.density;
		this.temperature = properties.temperature;
		this.viscosity = properties.viscosity;
		this.rarity = properties.rarity;
	}

	/** Called once on the client for each registered fluid type; hand back the client appearance. */
	public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {}

	public String getDescriptionId() {
		return descriptionId != null ? descriptionId : "fluid_type.unknown";
	}

	public String getDescriptionId(FluidStack stack) {
		return getDescriptionId();
	}

	public Component getDescription() {
		return Component.translatable(getDescriptionId());
	}

	public Component getDescription(FluidStack stack) {
		return Component.translatable(getDescriptionId(stack));
	}

	public double motionScale(Entity entity) {
		return motionScale;
	}

	public boolean canPushEntity(Entity entity) {
		return canPushEntity;
	}

	public boolean canSwim(Entity entity) {
		return canSwim;
	}

	public boolean canDrownIn(LivingEntity entity) {
		return canDrown;
	}

	public float getFallDistanceModifier(Entity entity) {
		return fallDistanceModifier;
	}

	public boolean canExtinguish(Entity entity) {
		return canExtinguish;
	}

	public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos) {
		return canConvertToSource;
	}

	public boolean supportsBoating(FluidState state) {
		return supportsBoating;
	}

	@Nullable
	public PathType getBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable net.minecraft.world.entity.Mob mob, boolean canFluidLog) {
		return canFluidLog ? pathType : null;
	}

	@Nullable
	public PathType getAdjacentBlockPathType(FluidState state, BlockGetter level, BlockPos pos, @Nullable net.minecraft.world.entity.Mob mob, PathType originalType) {
		return adjacentPathType;
	}

	@Nullable
	public SoundEvent getSound(SoundAction action) {
		return sounds.get(action);
	}

	@Nullable
	public SoundEvent getSound(FluidStack stack, SoundAction action) {
		return getSound(action);
	}

	public boolean canHydrate(FluidState state, BlockGetter getter, BlockPos pos, BlockState source, BlockPos sourcePos) {
		return canHydrate;
	}

	public int getLightLevel() {
		return lightLevel;
	}

	public int getLightLevel(FluidStack stack) {
		return getLightLevel();
	}

	public int getLightLevel(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
		return getLightLevel();
	}

	public int getDensity() {
		return density;
	}

	public int getDensity(FluidStack stack) {
		return getDensity();
	}

	public int getTemperature() {
		return temperature;
	}

	public int getTemperature(FluidStack stack) {
		return getTemperature();
	}

	public int getViscosity() {
		return viscosity;
	}

	public int getViscosity(FluidStack stack) {
		return getViscosity();
	}

	public Rarity getRarity() {
		return rarity;
	}

	public Rarity getRarity(FluidStack stack) {
		return getRarity();
	}

	/** The (filled) bucket for a stack of this type; delegates to the fluid's own bucket. */
	public ItemStack getBucket(FluidStack stack) {
		return new ItemStack(stack.getFluid().getBucket());
	}

	public boolean isAir() {
		return this == CommonFluids.EMPTY_TYPE.value();
	}

	public boolean isVanilla() {
		return this == CommonFluids.WATER_TYPE.value() || this == CommonFluids.LAVA_TYPE.value();
	}

	public boolean isLighterThanAir() {
		return density <= 0;
	}

	public boolean isVaporizedOnPlacement(net.minecraft.world.level.Level level, BlockPos pos, FluidStack stack) {
		return level.dimensionType().ultraWarm() && stack.is(net.minecraft.tags.FluidTags.WATER);
	}

	// ---- builder ----

	public static class Properties {
		private String descriptionId;
		private double motionScale = 0.014D;
		private boolean canPushEntity = true;
		private boolean canSwim = true;
		private boolean canDrown = true;
		private float fallDistanceModifier = 0.5F;
		private boolean canExtinguish = false;
		private boolean canConvertToSource = false;
		private boolean supportsBoating = false;
		@Nullable
		private PathType pathType = PathType.WATER;
		@Nullable
		private PathType adjacentPathType = PathType.WATER_BORDER;
		private final Map<SoundAction, SoundEvent> sounds = new HashMap<>();
		private boolean canHydrate = false;
		private int lightLevel = 0;
		private int density = 1000;
		private int temperature = 300;
		private int viscosity = 1000;
		private Rarity rarity = Rarity.COMMON;

		private Properties() {}

		public static Properties create() {
			return new Properties();
		}

		public Properties descriptionId(String descriptionId) {
			this.descriptionId = descriptionId;
			return this;
		}

		public Properties motionScale(double motionScale) {
			this.motionScale = motionScale;
			return this;
		}

		public Properties canPushEntity(boolean canPushEntity) {
			this.canPushEntity = canPushEntity;
			return this;
		}

		public Properties canSwim(boolean canSwim) {
			this.canSwim = canSwim;
			return this;
		}

		public Properties canDrown(boolean canDrown) {
			this.canDrown = canDrown;
			return this;
		}

		public Properties fallDistanceModifier(float fallDistanceModifier) {
			this.fallDistanceModifier = fallDistanceModifier;
			return this;
		}

		public Properties canExtinguish(boolean canExtinguish) {
			this.canExtinguish = canExtinguish;
			return this;
		}

		public Properties canConvertToSource(boolean canConvertToSource) {
			this.canConvertToSource = canConvertToSource;
			return this;
		}

		public Properties supportsBoating(boolean supportsBoating) {
			this.supportsBoating = supportsBoating;
			return this;
		}

		public Properties pathType(@Nullable PathType pathType) {
			this.pathType = pathType;
			return this;
		}

		public Properties adjacentPathType(@Nullable PathType adjacentPathType) {
			this.adjacentPathType = adjacentPathType;
			return this;
		}

		public Properties sound(SoundAction action, SoundEvent sound) {
			this.sounds.put(action, sound);
			return this;
		}

		public Properties canHydrate(boolean canHydrate) {
			this.canHydrate = canHydrate;
			return this;
		}

		public Properties lightLevel(int lightLevel) {
			this.lightLevel = lightLevel;
			return this;
		}

		public Properties density(int density) {
			this.density = density;
			return this;
		}

		public Properties temperature(int temperature) {
			this.temperature = temperature;
			return this;
		}

		public Properties viscosity(int viscosity) {
			this.viscosity = viscosity;
			return this;
		}

		public Properties rarity(Rarity rarity) {
			this.rarity = rarity;
			return this;
		}
	}
}
