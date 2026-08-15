package com.simibubi.create.foundation.mixin.extension;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.datamap.DataMapType;
import com.simibubi.create.foundation.datamap.DataMaps;

import net.minecraft.core.Holder;

/** Injected onto {@code Holder}: NeoForge's {@code Holder#getData(DataMapType)}. */
public interface HolderExtension {

	@Nullable
	@SuppressWarnings("unchecked")
	default <T> T getData(DataMapType<?, T> type) {
		Holder<Object> self = (Holder<Object>) this;
		return self.isBound() ? DataMaps.get((DataMapType<Object, T>) type, self.value()) : null;
	}
}
