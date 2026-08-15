package com.tterrag.registrate.providers;

import com.simibubi.create.foundation.data.modelgen.BlockStateProvider;
import com.simibubi.create.foundation.data.modelgen.ExistingFileHelper;

/**
 * Shell (see {@code com.simibubi.create.foundation.data.modelgen} package docs): upstream's blockstate
 * datagen provider surface, no-op bodies. Never instantiated in the port; exists so ported
 * {@code .blockstate((ctx, prov) -> ...)} lambdas compile.
 */
public class RegistrateBlockstateProvider extends BlockStateProvider {

	public RegistrateBlockstateProvider(String modid, ExistingFileHelper exFileHelper) {
		super(modid, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {}
}
