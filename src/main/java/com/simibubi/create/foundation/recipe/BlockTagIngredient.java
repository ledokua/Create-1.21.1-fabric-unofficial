package com.simibubi.create.foundation.recipe;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * NeoForge's {@code neoforge:block_tag} ingredient (items whose block is in a block tag) as a Fabric custom
 * ingredient, id {@code create:block_tag}. Upstream's recipe JSON is rewritten to it by scripts/convert-resources.py.
 */
public record BlockTagIngredient(TagKey<Block> tag) implements CustomIngredient {

	public static final MapCodec<BlockTagIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
		TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(BlockTagIngredient::tag)
	).apply(i, BlockTagIngredient::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> STREAM_CODEC = ResourceLocation.STREAM_CODEC
		.map(id -> new BlockTagIngredient(TagKey.create(Registries.BLOCK, id)), i -> i.tag().location());

	public static final CustomIngredientSerializer<BlockTagIngredient> SERIALIZER = new CustomIngredientSerializer<>() {
		@Override
		public ResourceLocation getIdentifier() {
			return Create.asResource("block_tag");
		}

		@Override
		public MapCodec<BlockTagIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlockTagIngredient> getPacketCodec() {
			return STREAM_CODEC;
		}
	};

	@Override
	public boolean test(ItemStack stack) {
		return stack.getItem() instanceof net.minecraft.world.item.BlockItem bi && bi.getBlock().builtInRegistryHolder().is(tag);
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return BuiltInRegistries.BLOCK.getTag(tag)
			.map(named -> named.stream().map(h -> new ItemStack(h.value())).filter(s -> !s.isEmpty()).toList())
			.orElseGet(List::of);
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
