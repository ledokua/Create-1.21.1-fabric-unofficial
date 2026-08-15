package com.simibubi.create.foundation.attachment;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

/** (De)serializer for a data attachment; the holder is available on read (Create-owned, was NeoForge's). */
public interface IAttachmentSerializer<S extends Tag, T> {

	T read(IAttachmentHolder holder, S tag, HolderLookup.Provider provider);

	@Nullable
	S write(T attachment, HolderLookup.Provider provider);
}
