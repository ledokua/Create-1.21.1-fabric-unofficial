package com.simibubi.create.compat.recipeViewer;

import java.util.List;

/** All slots of a laid-out recipe, for {@code draw}/{@code getTooltipStrings}. */
public interface RecipeSlotsView {
	List<SlotView> getSlotViews();

	default List<SlotView> getSlotViews(SlotRole role) {
		return getSlotViews().stream().filter(v -> v.getRole() == role).toList();
	}
}
