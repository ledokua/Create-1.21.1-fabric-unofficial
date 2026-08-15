package com.simibubi.create.foundation.transfer.capability.bridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;
import com.simibubi.create.foundation.transfer.fluid.IFluidHandler;
import com.simibubi.create.foundation.transfer.fluid.IFluidHandler.FluidAction;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Exposes an {@link IFluidHandler} (Create's tanks) to Fabric's Transfer API. Same simulate-then-apply-on-
 * commit approach (and limitation) as {@link ItemHandlerStorage}. Amounts convert mB ↔ droplets (×81);
 * sub-millibucket remainders are refused rather than rounded.
 */
public class FluidHandlerStorage implements Storage<FluidVariant> {

	private final IFluidHandler handler;

	public FluidHandlerStorage(IFluidHandler handler) {
		this.handler = handler;
	}

	public IFluidHandler handler() {
		return handler;
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext tx) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		int mb = Transactions.dropletsToMb(maxAmount);
		if (mb <= 0)
			return 0;
		FluidStack stack = StorageFluidHandler.toStack(resource, Transactions.mbToDroplets(mb));
		int filled = handler.fill(stack, FluidAction.SIMULATE);
		if (filled <= 0)
			return 0;
		FluidStack toFill = stack.copyWithAmount(filled);
		Transactions.applyOnCommit(tx, () -> handler.fill(toFill, FluidAction.EXECUTE));
		return Transactions.mbToDroplets(filled);
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);
		int mb = Transactions.dropletsToMb(maxAmount);
		if (mb <= 0)
			return 0;
		FluidStack request = StorageFluidHandler.toStack(resource, Transactions.mbToDroplets(mb));
		FluidStack drained = handler.drain(request, FluidAction.SIMULATE);
		if (drained.isEmpty())
			return 0;
		FluidStack toDrain = drained.copy();
		Transactions.applyOnCommit(tx, () -> handler.drain(toDrain, FluidAction.EXECUTE));
		return Transactions.mbToDroplets(drained.getAmount());
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator() {
		List<StorageView<FluidVariant>> views = new ArrayList<>(handler.getTanks());
		for (int i = 0; i < handler.getTanks(); i++)
			views.add(new Tank(i));
		return views.iterator();
	}

	private final class Tank implements StorageView<FluidVariant> {
		private final int index;

		Tank(int index) {
			this.index = index;
		}

		@Override
		public long extract(FluidVariant resource, long maxAmount, TransactionContext tx) {
			FluidStack inTank = handler.getFluidInTank(index);
			if (inTank.isEmpty() || !resource.equals(StorageFluidHandler.toVariant(inTank)))
				return 0;
			return FluidHandlerStorage.this.extract(resource, maxAmount, tx);
		}

		@Override
		public boolean isResourceBlank() {
			return handler.getFluidInTank(index).isEmpty();
		}

		@Override
		public FluidVariant getResource() {
			return StorageFluidHandler.toVariant(handler.getFluidInTank(index));
		}

		@Override
		public long getAmount() {
			return Transactions.mbToDroplets(handler.getFluidInTank(index).getAmount());
		}

		@Override
		public long getCapacity() {
			return Transactions.mbToDroplets(handler.getTankCapacity(index));
		}
	}
}
