package com.tterrag.registrate.providers.loot;

import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;

/**
 * Shell — see {@code RegistrateBlockstateProvider}. Extends vanilla's BlockLootSubProvider so every loot
 * helper ({@code dropSelf}, {@code createSlabItemTable}, {@code applyExplosionCondition}, ...) resolves.
 */
public class RegistrateBlockLootTables extends BlockLootSubProvider {

	public RegistrateBlockLootTables(HolderLookup.Provider provider) {
		super(Set.<Item>of(), FeatureFlags.REGISTRY.allFlags(), provider);
	}

	@Override
	public void generate() {}

	// vanilla keeps these protected; upstream widens them for lambdas
	@Override
	public net.minecraft.world.level.storage.loot.LootTable.Builder createSingleItemTable(net.minecraft.world.level.ItemLike item) {
		return super.createSingleItemTable(item);
	}

	@Override
	public net.minecraft.world.level.storage.loot.LootTable.Builder createSlabItemTable(net.minecraft.world.level.block.Block block) {
		return super.createSlabItemTable(block);
	}

	@Override
	public net.minecraft.world.level.storage.loot.LootTable.Builder createDoorTable(net.minecraft.world.level.block.Block block) {
		return super.createDoorTable(block);
	}

	@Override
	public net.minecraft.world.level.storage.loot.LootTable.Builder createSilkTouchDispatchTable(net.minecraft.world.level.block.Block block, net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder<?> builder) {
		return super.createSilkTouchDispatchTable(block, builder);
	}

	@Override
	public <T extends net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder<T>> T applyExplosionDecay(net.minecraft.world.level.ItemLike item, net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder<T> builder) {
		return super.applyExplosionDecay(item, builder);
	}

	@Override
	public <T extends net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder<T>> T applyExplosionCondition(net.minecraft.world.level.ItemLike item, net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder<T> builder) {
		return super.applyExplosionCondition(item, builder);
	}

	@Override
	public void dropSelf(net.minecraft.world.level.block.Block block) {
		super.dropSelf(block);
	}

	@Override
	public void dropOther(net.minecraft.world.level.block.Block block, net.minecraft.world.level.ItemLike item) {
		super.dropOther(block, item);
	}

	@Override
	public void dropWhenSilkTouch(net.minecraft.world.level.block.Block block) {
		super.dropWhenSilkTouch(block);
	}

	@Override
	public void add(net.minecraft.world.level.block.Block block, net.minecraft.world.level.storage.loot.LootTable.Builder builder) {
		super.add(block, builder);
	}

	public HolderLookup.Provider getRegistries() {
		return registries;
	}
}
