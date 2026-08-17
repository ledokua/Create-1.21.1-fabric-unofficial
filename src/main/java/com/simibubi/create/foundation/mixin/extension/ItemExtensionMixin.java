package com.simibubi.create.foundation.mixin.extension;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.simibubi.create.foundation.extension.ItemExtension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Attaches the Create-owned extension interface(s) at runtime. Loom's injected interfaces only exist at compile
 * time; without this mixin the game throws NoSuchMethodError outside the dev environment.
 * <p>
 * {@link #hasCustomEntity} and {@link #createEntity} are given concrete bodies here on purpose: Porting Lib
 * ({@code porting_lib_entity}, bundled by e.g. Twilight Forest) injects its own {@code ItemInjection} interface into
 * {@code Item} with default methods of exactly the same signatures. Two unrelated interfaces both supplying a default
 * for the same method makes every call throw {@code IncompatibleClassChangeError: Conflicting default methods}; a
 * method declared on the class itself satisfies both interfaces and ends the conflict. Item subclasses (PackageItem)
 * override these as before, and Porting Lib's own custom-entity hook then sees those overrides too.
 */
@Mixin(Item.class)
public abstract class ItemExtensionMixin implements ItemExtension {

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return false;
	}

	@Override
	@Nullable
	public Entity createEntity(Level level, Entity location, ItemStack stack) {
		return null;
	}
}
