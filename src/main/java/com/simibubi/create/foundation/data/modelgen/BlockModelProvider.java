package com.simibubi.create.foundation.data.modelgen;

/** Shell — see package docs. */
public abstract class BlockModelProvider extends ModelProvider<BlockModelBuilder> {

	public BlockModelProvider(String modid, ExistingFileHelper existingFileHelper) {
		super(modid, BLOCK_FOLDER, BlockModelBuilder::new, existingFileHelper);
	}
}
