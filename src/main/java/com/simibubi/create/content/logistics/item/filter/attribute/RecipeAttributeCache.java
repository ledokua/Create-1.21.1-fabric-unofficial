package com.simibubi.create.content.logistics.item.filter.attribute;

import java.lang.ref.WeakReference;
import java.util.function.BiPredicate;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenCustomHashMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

/**
 * Fabric port: remembers the answer of a recipe-based item attribute ("can be smelted", "can be washed", ...) per item
 * and components. Funnels and other filters test such attributes on every slot of the inventory they pull from, often
 * every tick, and each test is a recipe lookup over every recipe of the type. The answer only changes when the recipes
 * do, so the cache is dropped whenever the level's recipe manager is a different one (/reload, new world). Client and
 * server have separate caches.
 */
final class RecipeAttributeCache implements BiPredicate<ItemStack, Level> {
	private static final int MAX_ENTRIES = 4096;

	private final BiPredicate<ItemStack, Level> predicate;
	private final Side server = new Side();
	private final Side client = new Side();

	RecipeAttributeCache(BiPredicate<ItemStack, Level> predicate) {
		this.predicate = predicate;
	}

	@Override
	public boolean test(ItemStack stack, Level level) {
		if (stack.isEmpty() || level == null)
			return predicate.test(stack, level);
		return (level.isClientSide() ? client : server).test(stack, level);
	}

	private final class Side {
		private final Object2BooleanOpenCustomHashMap<ItemStack> results =
			new Object2BooleanOpenCustomHashMap<>(ItemStackLinkedSet.TYPE_AND_TAG);
		private WeakReference<RecipeManager> recipes = new WeakReference<>(null);

		synchronized boolean test(ItemStack stack, Level level) {
			RecipeManager manager = level.getRecipeManager();
			if (recipes.get() != manager) {
				results.clear();
				recipes = new WeakReference<>(manager);
			}
			if (results.containsKey(stack))
				return results.getBoolean(stack);
			boolean result = predicate.test(stack, level);
			if (results.size() >= MAX_ENTRIES)
				results.clear();
			results.put(stack.copyWithCount(1), result);
			return result;
		}
	}
}
