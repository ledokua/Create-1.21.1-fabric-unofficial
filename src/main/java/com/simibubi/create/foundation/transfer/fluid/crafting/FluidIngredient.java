package com.simibubi.create.foundation.transfer.fluid.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/**
 * Recipe-side predicate over {@link FluidStack}s (the fluid analogue of vanilla {@code Ingredient}).
 * Create-owned port of NeoForge's abstraction (see PORTING_PLAN.md, "NeoForge → Fabric translation
 * strategy"), with the same JSON shapes: an untyped {@code {"fluid": id}} / {@code {"tag": id}}, a
 * {@code "type"}-dispatched object, or a list (compound). Type ids are registered as {@code create:*} with
 * {@code neoforge:*} aliases so upstream's generated recipe JSON — and datapacks written for Create on
 * NeoForge — load unchanged.
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {

	// ---- type registry (static; small, closed set — no need for a full MC registry) ----

	private static final Map<ResourceLocation, FluidIngredientType<?>> TYPES = new LinkedHashMap<>();
	private static final Map<FluidIngredientType<?>, ResourceLocation> TYPE_IDS = new LinkedHashMap<>();

	public static <T extends FluidIngredient> FluidIngredientType<T> register(ResourceLocation id, FluidIngredientType<T> type, ResourceLocation... aliases) {
		TYPES.put(id, type);
		TYPE_IDS.putIfAbsent(type, id);
		for (ResourceLocation alias : aliases)
			TYPES.put(alias, type);
		return type;
	}

	private static ResourceLocation createId(String path) {
		return ResourceLocation.fromNamespaceAndPath("create", path);
	}

	private static ResourceLocation neoforgeAlias(String path) {
		return ResourceLocation.fromNamespaceAndPath("neoforge", path);
	}

	public static final Codec<FluidIngredientType<?>> TYPE_CODEC = ResourceLocation.CODEC.comapFlatMap(
		id -> {
			FluidIngredientType<?> type = TYPES.get(id);
			return type == null ? DataResult.error(() -> "Unknown fluid ingredient type: " + id) : DataResult.success(type);
		},
		type -> {
			ResourceLocation id = TYPE_IDS.get(type);
			if (id == null)
				throw new IllegalStateException("Unregistered fluid ingredient type " + type);
			return id;
		});

	static {
		register(createId("empty"), EmptyFluidIngredient.TYPE, neoforgeAlias("empty"));
		register(createId("single"), SingleFluidIngredient.TYPE, neoforgeAlias("single"));
		register(createId("tag"), TagFluidIngredient.TYPE, neoforgeAlias("tag"));
		register(createId("compound"), CompoundFluidIngredient.TYPE, neoforgeAlias("compound"));
		register(createId("components"), DataComponentFluidIngredient.TYPE, neoforgeAlias("components"));
	}

	// ---- codecs ----

	private static final MapCodec<FluidIngredient> SINGLE_OR_TAG_CODEC = MapCodec.recursive("FluidIngredient.SINGLE_OR_TAG_CODEC", self -> singleOrTagCodec());

	public static final MapCodec<FluidIngredient> MAP_CODEC_NONEMPTY = makeMapCodec();
	private static final Codec<FluidIngredient> MAP_CODEC_CODEC = MAP_CODEC_NONEMPTY.codec();

	public static final Codec<List<FluidIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
	public static final Codec<List<FluidIngredient>> LIST_CODEC_NON_EMPTY = LIST_CODEC.validate(list -> list.isEmpty()
		? DataResult.error(() -> "Fluid ingredient cannot be empty, at least one item must be defined")
		: DataResult.success(list));

	public static final Codec<FluidIngredient> CODEC = codec(true);
	public static final Codec<FluidIngredient> CODEC_NON_EMPTY = codec(false);

	public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
		private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidStack>> FLUID_LIST_CODEC = FluidStack.STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));

		@Override
		public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
			if (ingredient.isSimple()) {
				FLUID_LIST_CODEC.encode(buf, Arrays.asList(ingredient.getStacks()));
			} else {
				buf.writeVarInt(-1);
				encodeDispatched(buf, ingredient);
			}
		}

		@SuppressWarnings("unchecked")
		private static <T extends FluidIngredient> void encodeDispatched(RegistryFriendlyByteBuf buf, T ingredient) {
			FluidIngredientType<T> type = (FluidIngredientType<T>) ingredient.getType();
			buf.writeResourceLocation(TYPE_IDS.get(type));
			type.streamCodec().encode(buf, ingredient);
		}

		@Override
		public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
			int size = buf.readVarInt();
			if (size == -1) {
				FluidIngredientType<?> type = TYPES.get(buf.readResourceLocation());
				return type.streamCodec().decode(buf);
			}
			return CompoundFluidIngredient.of(Stream.generate(() -> FluidStack.STREAM_CODEC.decode(buf)).limit(size).map(FluidIngredient::single));
		}
	};

	private static MapCodec<FluidIngredient> singleOrTagCodec() {
		return Codec.mapEither(SingleFluidIngredient.CODEC, TagFluidIngredient.CODEC).xmap(
			either -> either.map(id -> id, id -> id),
			ingredient -> {
				if (ingredient instanceof SingleFluidIngredient fluid)
					return Either.left(fluid);
				if (ingredient instanceof TagFluidIngredient tag)
					return Either.right(tag);
				throw new IllegalStateException("Basic fluid ingredient should be either a fluid or a tag!");
			});
	}

	private static MapCodec<FluidIngredient> makeMapCodec() {
		MapCodec<FluidIngredient> dispatched = TYPE_CODEC.dispatchMap("type", FluidIngredient::getType, type -> ((FluidIngredientType<FluidIngredient>) type).codec());
		// "type" present → dispatch; absent → bare single-or-tag. Serialize simple ones without a type field.
		return new MapCodec<FluidIngredient>() {
			@Override
			public <T> Stream<T> keys(com.mojang.serialization.DynamicOps<T> ops) {
				return Stream.concat(dispatched.keys(ops), SINGLE_OR_TAG_CODEC.keys(ops)).distinct();
			}

			@Override
			public <T> DataResult<FluidIngredient> decode(com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.MapLike<T> input) {
				if (input.get("type") != null)
					return dispatched.decode(ops, input);
				return SINGLE_OR_TAG_CODEC.decode(ops, input);
			}

			@Override
			public <T> com.mojang.serialization.RecordBuilder<T> encode(FluidIngredient input, com.mojang.serialization.DynamicOps<T> ops, com.mojang.serialization.RecordBuilder<T> prefix) {
				if (input instanceof SingleFluidIngredient || input instanceof TagFluidIngredient)
					return SINGLE_OR_TAG_CODEC.encode(input, ops, prefix);
				return dispatched.encode(input, ops, prefix);
			}
		}.validate(ingredient -> ingredient.isEmpty()
			? DataResult.error(() -> "Cannot serialize empty fluid ingredient using the map codec")
			: DataResult.success(ingredient));
	}

	private static Codec<FluidIngredient> codec(boolean allowEmpty) {
		Codec<List<FluidIngredient>> listCodec = Codec.lazyInitialized(() -> allowEmpty ? LIST_CODEC : LIST_CODEC_NON_EMPTY);
		return Codec.either(listCodec, MAP_CODEC_CODEC).xmap(
			either -> either.map(CompoundFluidIngredient::of, i -> i),
			ingredient -> {
				if (ingredient instanceof CompoundFluidIngredient compound)
					return Either.left(compound.children());
				if (ingredient.isEmpty())
					return Either.left(List.of());
				return Either.right(ingredient);
			});
	}

	// ---- instance ----

	@Nullable
	private FluidStack[] stacks;

	/** Representative stacks (for display / simple network sync); cached. */
	public final FluidStack[] getStacks() {
		if (stacks == null)
			stacks = generateStacks().toArray(FluidStack[]::new);
		return stacks;
	}

	@Override
	public abstract boolean test(FluidStack fluidStack);

	protected abstract Stream<FluidStack> generateStacks();

	/** Simple = fully described by {@link #getStacks()} (fluid + components), so it can be synced as a plain list. */
	public abstract boolean isSimple();

	public abstract FluidIngredientType<?> getType();

	public final boolean isEmpty() {
		return this == EmptyFluidIngredient.INSTANCE;
	}

	public final boolean hasNoFluids() {
		return getStacks().length == 0;
	}

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);

	// ---- factories ----

	public static FluidIngredient empty() {
		return EmptyFluidIngredient.INSTANCE;
	}

	public static FluidIngredient of() {
		return empty();
	}

	public static FluidIngredient of(FluidStack... fluids) {
		return of(Arrays.stream(fluids).map(FluidStack::getFluid));
	}

	public static FluidIngredient of(Fluid... fluids) {
		return of(Arrays.stream(fluids));
	}

	private static FluidIngredient of(Stream<Fluid> fluids) {
		List<FluidIngredient> children = new ArrayList<>();
		fluids.forEach(f -> children.add(single(f)));
		return CompoundFluidIngredient.of(children);
	}

	public static FluidIngredient single(FluidStack stack) {
		return single(stack.getFluid());
	}

	public static FluidIngredient single(Fluid fluid) {
		return single(fluid.builtInRegistryHolder());
	}

	public static FluidIngredient single(Holder<Fluid> holder) {
		return new SingleFluidIngredient(holder);
	}

	public static FluidIngredient tag(TagKey<Fluid> tag) {
		return new TagFluidIngredient(tag);
	}
}
