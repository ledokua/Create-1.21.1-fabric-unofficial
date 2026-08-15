package com.simibubi.create.foundation.transfer.fluid;

import com.simibubi.create.foundation.mixin.accessor.BucketItemAccessor;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Treats a (possibly empty) vanilla-style bucket stack as a one-bucket fluid handler. Milk is not a fluid
 * on Fabric (no vanilla fluid exists; mods use the {@code c:milk} tag by convention) — a milk bucket reads as
 * empty here, matching upstream's behaviour minus NeoForge's synthetic milk fluid. See the Phase 2 fluid
 * design in PORTING_PLAN.md if that ever matters.
 */
public class FluidBucketWrapper implements IFluidHandlerItem {

	protected ItemStack container;

	public FluidBucketWrapper(ItemStack container) {
		this.container = container;
	}

	@Override
	public ItemStack getContainer() {
		return container;
	}

	public boolean canFillFluidType(FluidStack fluid) {
		return fluid.getFluid().getBucket() != Items.AIR;
	}

	public FluidStack getFluid() {
		Item item = container.getItem();
		if (item instanceof BucketItem bucket) {
			Fluid content = ((BucketItemAccessor) bucket).create$getContent();
			if (content != Fluids.EMPTY)
				return new FluidStack(content, FluidStack.BUCKET_VOLUME);
		}
		return FluidStack.EMPTY;
	}

	protected void setFluid(FluidStack fluidStack) {
		if (fluidStack.isEmpty())
			container = new ItemStack(Items.BUCKET);
		else
			container = new ItemStack(fluidStack.getFluid().getBucket());
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return getFluid();
	}

	@Override
	public int getTankCapacity(int tank) {
		return FluidStack.BUCKET_VOLUME;
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return true;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (container.getCount() != 1 || resource.getAmount() < FluidStack.BUCKET_VOLUME || !getFluid().isEmpty()
			|| !canFillFluidType(resource))
			return 0;
		if (action.execute())
			setFluid(resource);
		return FluidStack.BUCKET_VOLUME;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (container.getCount() != 1 || resource.getAmount() < FluidStack.BUCKET_VOLUME)
			return FluidStack.EMPTY;
		FluidStack fluidStack = getFluid();
		if (!fluidStack.isEmpty() && FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
			if (action.execute())
				setFluid(FluidStack.EMPTY);
			return fluidStack;
		}
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (container.getCount() != 1 || maxDrain < FluidStack.BUCKET_VOLUME)
			return FluidStack.EMPTY;
		FluidStack fluidStack = getFluid();
		if (!fluidStack.isEmpty()) {
			if (action.execute())
				setFluid(FluidStack.EMPTY);
			return fluidStack;
		}
		return FluidStack.EMPTY;
	}
}
