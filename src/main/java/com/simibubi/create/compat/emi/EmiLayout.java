package com.simibubi.create.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.recipeViewer.RecipeLayoutBuilder;
import com.simibubi.create.compat.recipeViewer.RecipeSlot;
import com.simibubi.create.compat.recipeViewer.RecipeSlotsView;
import com.simibubi.create.compat.recipeViewer.SlotItemRenderer;
import com.simibubi.create.compat.recipeViewer.SlotRole;
import com.simibubi.create.compat.recipeViewer.SlotTooltipCallback;
import com.simibubi.create.compat.recipeViewer.SlotView;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.transfer.fluid.FluidStack;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.world.item.ItemStack;

/**
 * Collects a category's slot layout (viewer-neutral API) into plain descriptions; {@link CreateEmiRecipe} derives
 * inputs/outputs from them and turns them into EMI widgets on demand.
 */
@Environment(EnvType.CLIENT)
public class EmiLayout implements RecipeLayoutBuilder, RecipeSlotsView {

	public static final long DROPLETS_PER_MB = FluidConstants.BUCKET / 1000L;

	public final List<Slot> slots = new ArrayList<>();

	@Override
	public RecipeSlot addSlot(SlotRole role, int x, int y) {
		Slot slot = new Slot(role, x, y, true);
		slots.add(slot);
		return slot;
	}

	@Override
	public RecipeSlot addInvisibleIngredients(SlotRole role) {
		Slot slot = new Slot(role, 0, 0, false);
		slots.add(slot);
		return slot;
	}

	@Override
	public List<SlotView> getSlotViews() {
		return new ArrayList<>(slots);
	}

	public static EmiStack toEmi(FluidStack stack) {
		return EmiStack.of(stack.getFluid(), stack.getComponentsPatch(), stack.getAmount() * DROPLETS_PER_MB);
	}

	public static FluidStack fromEmi(EmiStack stack) {
		if (!(stack.getKey() instanceof net.minecraft.world.level.material.Fluid fluid))
			return FluidStack.EMPTY;
		return new FluidStack(fluid.builtInRegistryHolder(), (int) Math.max(1, stack.getAmount() / DROPLETS_PER_MB), stack.getComponentChanges());
	}

	@Environment(EnvType.CLIENT)
	public static class Slot implements RecipeSlot, SlotView {
		public final SlotRole role;
		public final int x, y;
		public final boolean visible;
		public final List<EmiStack> stacks = new ArrayList<>();
		public final List<SlotTooltipCallback> tooltipCallbacks = new ArrayList<>();
		@Nullable
		public AllGuiTextures background;
		public int backgroundDx, backgroundDy;
		@Nullable
		public SlotItemRenderer customRenderer;
		public boolean fluid;

		Slot(SlotRole role, int x, int y, boolean visible) {
			this.role = role;
			this.x = x;
			this.y = y;
			this.visible = visible;
		}

		public EmiIngredient ingredient() {
			if (stacks.isEmpty())
				return EmiStack.EMPTY;
			return stacks.size() == 1 ? stacks.get(0) : EmiIngredient.of(stacks);
		}

		@Override
		public RecipeSlot addItemStack(ItemStack stack) {
			if (!stack.isEmpty())
				stacks.add(EmiStack.of(stack));
			return this;
		}

		@Override
		public RecipeSlot addItemStacks(List<ItemStack> list) {
			list.forEach(this::addItemStack);
			return this;
		}

		@Override
		public RecipeSlot addFluids(List<FluidStack> fluids, int displayAmount) {
			fluid = true;
			for (FluidStack f : fluids)
				if (!f.isEmpty())
					stacks.add(EmiStack.of(f.getFluid(), f.getComponentsPatch(), displayAmount * DROPLETS_PER_MB));
			return this;
		}

		@Override
		public RecipeSlot setBackground(AllGuiTextures texture, int dx, int dy) {
			background = texture;
			backgroundDx = dx;
			backgroundDy = dy;
			return this;
		}

		@Override
		public RecipeSlot addTooltipCallback(SlotTooltipCallback callback) {
			tooltipCallbacks.add(callback);
			return this;
		}

		@Override
		public RecipeSlot setCustomRenderer(SlotItemRenderer renderer) {
			customRenderer = renderer;
			return this;
		}

		// ---- SlotView (static: EMI cycles ingredients itself; the first alternative stands in) ----

		@Override
		public SlotRole getRole() {
			return role;
		}

		@Override
		public Optional<ItemStack> getDisplayedItem() {
			return stacks.isEmpty() || fluid ? Optional.empty() : Optional.of(stacks.get(0).getItemStack());
		}

		@Override
		public Optional<FluidStack> getDisplayedFluid() {
			return stacks.isEmpty() || !fluid ? Optional.empty() : Optional.of(fromEmi(stacks.get(0)));
		}
	}
}
