/**
 * <b>Shell</b> of NeoForge's model-generation datagen DSL ({@code net.neoforged.neoforge.client.model.generators}
 * + {@code ExistingFileHelper}) with upstream's signatures and <b>no-op bodies</b>. Create's ~320 vendored
 * datagen lambdas (`.blockstate((c, p) -> p.simpleBlock(...))` etc.) compile against this but are never
 * executed — the mod ships upstream's pre-generated JSON instead. Phase 11 replaces this with real Fabric
 * datagen. See PORTING_PLAN.md, "NeoForge → Fabric translation strategy" and Phase 0b.
 */
package com.simibubi.create.foundation.data.modelgen;
