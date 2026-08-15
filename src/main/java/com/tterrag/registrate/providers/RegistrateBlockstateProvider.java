package com.tterrag.registrate.providers;

/**
 * Placeholder for Phase 11. Ported call sites that chain {@code .blockstate((ctx, prov) -> ...)} need this
 * type to exist for the lambda to compile, but the callback is never invoked yet (see {@code ProviderType}
 * and {@code Builder.setData}) — flesh out real methods here on demand as Phase 2 content porting hits
 * specific ones, rather than speculatively reimplementing NeoForge's BlockStateProvider surface now.
 */
public class RegistrateBlockstateProvider {
}
