package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * NeoForge's {@code ShapedRecipePattern.setCraftingSize(9, 9)}: Create's mechanical crafting recipes are up to 9x9.
 * The row/column limit lives in a codec lambda of {@code ShapedRecipePattern.Data} (intermediary-named, they have
 * no Mojang name); the constant is vanilla's inlined {@code MAX_SIZE = 3}.
 */
@Mixin(ShapedRecipePattern.Data.class)
public class ShapedRecipePatternDataMixin {
	@ModifyConstant(method = "method_55096", constant = @Constant(intValue = 3), remap = false)
	private static int create$maxCraftingSize(int original) {
		return 9;
	}
}
