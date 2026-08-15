package com.simibubi.create.foundation.model.data;

import java.util.function.Predicate;

/**
 * Typed key into a {@link ModelData}. Create-owned (was NeoForge's) — see PORTING_PLAN.md,
 * "NeoForge → Fabric translation strategy". Identity-compared.
 */
public final class ModelProperty<T> implements Predicate<T> {

	private final Predicate<T> validator;

	public ModelProperty() {
		this(t -> true);
	}

	public ModelProperty(Predicate<T> validator) {
		this.validator = validator;
	}

	@Override
	public boolean test(T t) {
		return validator.test(t);
	}
}
