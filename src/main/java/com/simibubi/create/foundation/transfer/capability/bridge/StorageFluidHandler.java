package com.simibubi.create.foundation.transfer.capability.bridge;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.transfer.fluid.FluidStack;
import com.simibubi.create.foundation.transfer.fluid.IFluidHandler;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

/**
 * {@link IFluidHandler} view of a Fabric {@code Storage<FluidVariant>} (other mods' tanks, cauldrons via
 * Fabric's cauldron storage, ...). Tanks are the storage's views in iteration order; amounts convert between
 * millibuckets and droplets (×81).
 */
public class StorageFluidHandler implements IFluidHandler {

	protected final Storage<FluidVariant> storage;

	public StorageFluidHandler(Storage<FluidVariant> storage) {
		this.storage = storage;
	}

	public Storage<FluidVariant> storage() {
		return storage;
	}

	protected List<StorageView<FluidVariant>> views() {
		List<StorageView<FluidVariant>> list = new ArrayList<>();
		for (StorageView<FluidVariant> view : storage)
			list.add(view);
		return list;
	}

	static FluidStack toStack(FluidVariant variant, long droplets) {
		if (variant.isBlank() || droplets <= 0)
			return FluidStack.EMPTY;
		return new FluidStack(variant.getFluid().builtInRegistryHolder(), Transactions.dropletsToMb(droplets), variant.getComponents());
	}

	static FluidVariant toVariant(FluidStack stack) {
		return stack.isEmpty() ? FluidVariant.blank() : FluidVariant.of(stack.getFluid(), stack.getComponentsPatch());
	}

	@Override
	public int getTanks() {
		return Math.max(1, views().size());
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		List<StorageView<FluidVariant>> views = views();
		if (tank < 0 || tank >= views.size())
			return FluidStack.EMPTY;
		StorageView<FluidVariant> view = views.get(tank);
		return toStack(view.getResource(), view.getAmount());
	}

	@Override
	public int getTankCapacity(int tank) {
		List<StorageView<FluidVariant>> views = views();
		if (tank < 0 || tank >= views.size())
			return 0;
		return Transactions.dropletsToMb(views.get(tank).getCapacity());
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return storage.supportsInsertion();
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !storage.supportsInsertion())
			return 0;
		try (Transaction tx = Transactions.open()) {
			long inserted = storage.insert(toVariant(resource), Transactions.mbToDroplets(resource.getAmount()), tx);
			int mb = Transactions.dropletsToMb(inserted);
			if (action.execute())
				tx.commit();
			return mb;
		}
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !storage.supportsExtraction())
			return FluidStack.EMPTY;
		FluidVariant variant = toVariant(resource);
		try (Transaction tx = Transactions.open()) {
			long extracted = storage.extract(variant, Transactions.mbToDroplets(resource.getAmount()), tx);
			int mb = Transactions.dropletsToMb(extracted);
			if (mb <= 0)
				return FluidStack.EMPTY;
			if (action.execute())
				tx.commit();
			return resource.copyWithAmount(mb);
		}
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (maxDrain <= 0 || !storage.supportsExtraction())
			return FluidStack.EMPTY;
		for (StorageView<FluidVariant> view : storage) {
			if (view.isResourceBlank())
				continue;
			FluidVariant variant = view.getResource();
			try (Transaction tx = Transactions.open()) {
				long extracted = storage.extract(variant, Transactions.mbToDroplets(maxDrain), tx);
				int mb = Transactions.dropletsToMb(extracted);
				if (mb <= 0)
					continue;
				if (action.execute())
					tx.commit();
				return toStack(variant, Transactions.mbToDroplets(mb));
			}
		}
		return FluidStack.EMPTY;
	}
}
