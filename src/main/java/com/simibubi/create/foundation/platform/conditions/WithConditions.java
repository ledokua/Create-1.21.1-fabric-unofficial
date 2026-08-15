package com.simibubi.create.foundation.platform.conditions;

import java.util.List;

/** A value bundled with the conditions under which it should load. */
public record WithConditions<A>(List<ICondition> conditions, A carrier) {

	public WithConditions(A carrier) {
		this(List.of(), carrier);
	}
}
