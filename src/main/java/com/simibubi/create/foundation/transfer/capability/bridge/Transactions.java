package com.simibubi.create.foundation.transfer.capability.bridge;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/** Small helpers shared by the bridge adapters. */
final class Transactions {

	private Transactions() {}

	/** Opens a transaction that nests correctly whether or not one is already open on this thread. */
	static Transaction open() {
		return Transaction.isOpen() ? Transaction.openNested(Transaction.getCurrentUnsafe()) : Transaction.openOuter();
	}

	/**
	 * Schedules {@code action} to run when the outermost transaction commits, unless {@code tx} (or any
	 * transaction between it and the outermost) is aborted first. This is how a non-transactional
	 * NeoForge-style handler participates in a Fabric transaction: simulate now, apply on commit.
	 */
	static void applyOnCommit(TransactionContext tx, Runnable action) {
		boolean[] cancelled = new boolean[1];
		// walk from this transaction outwards; an abort at any level cancels
		for (int depth = tx.nestingDepth(); depth >= 0; depth--) {
			tx.getOpenTransaction(depth).addCloseCallback((t, result) -> {
				if (result.wasAborted())
					cancelled[0] = true;
			});
		}
		tx.addOuterCloseCallback(result -> {
			if (result.wasCommitted() && !cancelled[0])
				action.run();
		});
	}

	static final long DROPLETS_PER_MB = 81; // FluidConstants.BUCKET (81000) / 1000 mB

	static long mbToDroplets(long mb) {
		return mb * DROPLETS_PER_MB;
	}

	static int dropletsToMb(long droplets) {
		return (int) Math.min(Integer.MAX_VALUE, droplets / DROPLETS_PER_MB);
	}
}
