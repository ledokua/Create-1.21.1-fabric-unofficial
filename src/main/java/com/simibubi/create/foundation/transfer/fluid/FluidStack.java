package com.simibubi.create.foundation.transfer.fluid;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * An amount of a fluid plus data components — the fluid analogue of {@code ItemStack}. Create-owned port of
 * the abstraction upstream gets from NeoForge (see PORTING_PLAN.md, "NeoForge → Fabric translation
 * strategy"). Serialized shape ({@code id}/{@code amount}/{@code components}, NBT and network) matches
 * upstream's so vendored recipe JSON and existing save data load unchanged. Amounts are in millibuckets
 * (1000 per bucket), same as upstream; conversion to Fabric's droplets (81000 per bucket) happens only in the
 * Transfer API bridge.
 */
public final class FluidStack implements DataComponentHolder {

	public static final int BUCKET_VOLUME = 1000;

	public static final FluidStack EMPTY = new FluidStack((Void) null);

	public static final Codec<Holder<Fluid>> FLUID_NON_EMPTY_CODEC = BuiltInRegistries.FLUID.holderByNameCodec()
		.validate(holder -> holder.is(Fluids.EMPTY.builtInRegistryHolder())
			? DataResult.error(() -> "Fluid must not be minecraft:empty")
			: DataResult.success(holder));

	public static final Codec<FluidStack> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(i -> i.group(
		FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
		ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(FluidStack::getAmount),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStack::getComponentsPatch))
		.apply(i, FluidStack::new)));

	/** Like {@link #CODEC} but encodes/decodes {@link #EMPTY} as an empty map. */
	public static final Codec<FluidStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
		.xmap(o -> o.orElse(EMPTY), s -> s.isEmpty() ? Optional.empty() : Optional.of(s));

	public static Codec<FluidStack> fixedAmountCodec(int amount) {
		return Codec.lazyInitialized(() -> RecordCodecBuilder.create(i -> i.group(
			FLUID_NON_EMPTY_CODEC.fieldOf("id").forGetter(FluidStack::getFluidHolder),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidStack::getComponentsPatch))
			.apply(i, (holder, patch) -> new FluidStack(holder, amount, patch))));
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
		private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_STREAM_CODEC = ByteBufCodecs.holderRegistry(net.minecraft.core.registries.Registries.FLUID);

		@Override
		public FluidStack decode(RegistryFriendlyByteBuf buf) {
			int amount = buf.readVarInt();
			if (amount <= 0)
				return EMPTY;
			Holder<Fluid> holder = FLUID_STREAM_CODEC.decode(buf);
			DataComponentPatch patch = DataComponentPatch.STREAM_CODEC.decode(buf);
			return new FluidStack(holder, amount, patch);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
			if (stack.isEmpty()) {
				buf.writeVarInt(0);
				return;
			}
			buf.writeVarInt(stack.getAmount());
			FLUID_STREAM_CODEC.encode(buf, stack.getFluidHolder());
			DataComponentPatch.STREAM_CODEC.encode(buf, stack.getComponentsPatch());
		}
	};

	/** Like {@link #OPTIONAL_STREAM_CODEC} but refuses to encode/decode {@link #EMPTY}. */
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidStack> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public FluidStack decode(RegistryFriendlyByteBuf buf) {
			FluidStack stack = OPTIONAL_STREAM_CODEC.decode(buf);
			if (stack.isEmpty())
				throw new IllegalStateException("Empty FluidStack not allowed");
			return stack;
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, FluidStack stack) {
			if (stack.isEmpty())
				throw new IllegalStateException("Empty FluidStack not allowed");
			OPTIONAL_STREAM_CODEC.encode(buf, stack);
		}
	};

	@Nullable
	private final Fluid fluid;
	@Nullable
	private final Holder<Fluid> holder;
	private int amount;
	private final PatchedDataComponentMap components;

	private FluidStack(@Nullable Void unused) { // EMPTY only
		this.fluid = null;
		this.holder = null;
		this.amount = 0;
		this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
	}

	public FluidStack(Holder<Fluid> fluid, int amount, DataComponentPatch patch) {
		this(fluid, amount, PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, patch));
	}

	public FluidStack(Holder<Fluid> fluid, int amount) {
		this(fluid, amount, new PatchedDataComponentMap(DataComponentMap.EMPTY));
	}

	public FluidStack(Fluid fluid, int amount) {
		this(fluid.builtInRegistryHolder(), amount);
	}

	private FluidStack(Holder<Fluid> holder, int amount, PatchedDataComponentMap components) {
		Objects.requireNonNull(holder, "fluid");
		this.holder = holder;
		this.fluid = holder.value();
		this.amount = amount;
		this.components = components;
	}

	// ---- (de)serialization ----

	public static Optional<FluidStack> parse(HolderLookup.Provider lookupProvider, Tag tag) {
		return CODEC.parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), tag)
			.resultOrPartial(err -> com.simibubi.create.Create.LOGGER.error("Tried to load invalid fluid: '{}'", err));
	}

	public static FluidStack parseOptional(HolderLookup.Provider lookupProvider, CompoundTag tag) {
		return tag.isEmpty() ? EMPTY : parse(lookupProvider, tag).orElse(EMPTY);
	}

	public Tag save(HolderLookup.Provider lookupProvider, Tag prefix) {
		if (isEmpty())
			throw new IllegalStateException("Cannot encode empty FluidStack");
		return CODEC.encode(this, lookupProvider.createSerializationContext(NbtOps.INSTANCE), prefix).getOrThrow();
	}

	public Tag save(HolderLookup.Provider lookupProvider) {
		if (isEmpty())
			throw new IllegalStateException("Cannot encode empty FluidStack");
		return CODEC.encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
	}

	public Tag saveOptional(HolderLookup.Provider lookupProvider) {
		return isEmpty() ? new CompoundTag() : save(lookupProvider, new CompoundTag());
	}

	public static MapCodec<FluidStack> lenientOptionalFieldOf(String fieldName) {
		return CODEC.lenientOptionalFieldOf(fieldName).xmap(o -> o.orElse(EMPTY),
			s -> s.isEmpty() ? Optional.empty() : Optional.of(s));
	}

	// ---- components ----

	@Override
	public DataComponentMap getComponents() {
		return components;
	}

	public DataComponentPatch getComponentsPatch() {
		return isEmpty() ? DataComponentPatch.EMPTY : components.asPatch();
	}

	public boolean isComponentsPatchEmpty() {
		return isEmpty() || components.asPatch().isEmpty();
	}

	@Nullable
	public <T> T set(DataComponentType<? super T> type, @Nullable T component) {
		return components.set(type, component);
	}

	@Nullable
	public <T> T remove(DataComponentType<? extends T> type) {
		return components.remove(type);
	}

	public void applyComponents(DataComponentPatch patch) {
		components.applyPatch(patch);
	}

	public void applyComponents(DataComponentMap map) {
		components.setAll(map);
	}

	// ---- fluid / amount ----

	public boolean isEmpty() {
		return this == EMPTY || fluid == null || fluid == Fluids.EMPTY || amount <= 0;
	}

	public Fluid getFluid() {
		return isEmpty() ? Fluids.EMPTY : fluid;
	}

	public Holder<Fluid> getFluidHolder() {
		return isEmpty() ? Fluids.EMPTY.builtInRegistryHolder() : holder;
	}

	public int getAmount() {
		return isEmpty() ? 0 : amount;
	}

	public void setAmount(int amount) {
		if (this == EMPTY)
			throw new UnsupportedOperationException("Can't modify the empty stack.");
		this.amount = amount;
	}

	public void limitSize(int amount) {
		if (!isEmpty() && getAmount() > amount)
			setAmount(amount);
	}

	public void grow(int addedAmount) {
		setAmount(getAmount() + addedAmount);
	}

	public void shrink(int removedAmount) {
		setAmount(getAmount() - removedAmount);
	}

	/** Removes up to {@code amount} from this stack and returns it as a new stack. */
	public FluidStack split(int amount) {
		int i = Math.min(amount, getAmount());
		FluidStack copy = copyWithAmount(i);
		shrink(i);
		return copy;
	}

	public FluidStack copyAndClear() {
		if (isEmpty())
			return EMPTY;
		FluidStack copy = copy();
		setAmount(0);
		return copy;
	}

	public FluidStack copy() {
		if (isEmpty())
			return EMPTY;
		return new FluidStack(holder, amount, components.copy());
	}

	public FluidStack copyWithAmount(int amount) {
		if (isEmpty())
			return EMPTY;
		FluidStack copy = copy();
		copy.setAmount(amount);
		return copy;
	}

	// ---- queries ----

	public boolean is(TagKey<Fluid> tag) {
		return getFluidHolder().is(tag);
	}

	public boolean is(Fluid fluid) {
		return getFluid() == fluid;
	}

	public boolean is(Predicate<Holder<Fluid>> holderPredicate) {
		return holderPredicate.test(getFluidHolder());
	}

	public boolean is(Holder<Fluid> holder) {
		return getFluidHolder() == holder;
	}

	public boolean is(HolderSet<Fluid> holderSet) {
		return holderSet.contains(getFluidHolder());
	}

	public Stream<TagKey<Fluid>> getTags() {
		return getFluidHolder().tags();
	}

	public String getDescriptionId() {
		return FluidText.descriptionId(getFluid());
	}

	public Component getHoverName() {
		return FluidText.name(this);
	}

	/** Same fluid + components + amount. */
	public static boolean matches(FluidStack first, FluidStack second) {
		if (first == second)
			return true;
		return first.getAmount() == second.getAmount() && isSameFluidSameComponents(first, second);
	}

	public static boolean isSameFluid(FluidStack first, FluidStack second) {
		return first.is(second.getFluid());
	}

	/** Same fluid + components, amount ignored (the usual "can these merge" check). */
	public static boolean isSameFluidSameComponents(FluidStack first, FluidStack second) {
		if (!first.is(second.getFluid()))
			return false;
		if (first.isEmpty() && second.isEmpty())
			return true;
		return Objects.equals(first.components, second.components);
	}

	public static int hashFluidAndComponents(@Nullable FluidStack stack) {
		if (stack == null || stack.isEmpty())
			return 0;
		return 31 * stack.getFluid().hashCode() + stack.components.hashCode();
	}

	@Override
	public String toString() {
		return getAmount() + " " + BuiltInRegistries.FLUID.getKey(getFluid());
	}
}
