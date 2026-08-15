#!/usr/bin/env bash
# Mechanical, re-runnable NeoForge -> Fabric source rewrites for vendored upstream Create code.
# Run on a directory: scripts/neoforge-to-fabric.sh src/main/java/com/simibubi/create/foundation
# Keep this purely textual & idempotent so it can be re-applied after syncing from upstream.
set -euo pipefail
target="${1:?dir}"
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/^import net\.neoforged\.api\.distmarker\.Dist;$/import net.fabricmc.api.EnvType;/' \
  -e 's/^import net\.neoforged\.api\.distmarker\.OnlyIn;$/import net.fabricmc.api.Environment;/' \
  -e 's/@OnlyIn\(Dist\.CLIENT\)/@Environment(EnvType.CLIENT)/g' \
  -e 's/@OnlyIn\(Dist\.DEDICATED_SERVER\)/@Environment(EnvType.SERVER)/g' \
  -e 's/@OnlyIn\(value = Dist\.CLIENT\)/@Environment(EnvType.CLIENT)/g' \
  -e 's/\bDist\.CLIENT\b/EnvType.CLIENT/g' \
  -e 's/\bDist\.DEDICATED_SERVER\b/EnvType.SERVER/g'

# ---- item/fluid handler API + capabilities -> Create-owned com.simibubi.create.foundation.transfer.* ----
# (see PORTING_PLAN.md, "NeoForge -> Fabric translation strategy")
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.items\.wrapper\./com.simibubi.create.foundation.transfer.item.wrapper./g' \
  -e 's/net\.neoforged\.neoforge\.items\./com.simibubi.create.foundation.transfer.item./g' \
  -e 's/net\.neoforged\.neoforge\.fluids\.capability\.templates\./com.simibubi.create.foundation.transfer.fluid./g' \
  -e 's/net\.neoforged\.neoforge\.fluids\.capability\.wrappers\./com.simibubi.create.foundation.transfer.fluid./g' \
  -e 's/net\.neoforged\.neoforge\.fluids\.capability\./com.simibubi.create.foundation.transfer.fluid./g' \
  -e 's/net\.neoforged\.neoforge\.fluids\.crafting\./com.simibubi.create.foundation.transfer.fluid.crafting./g' \
  -e 's/net\.neoforged\.neoforge\.fluids\.(FluidStack|IFluidTank)\b/com.simibubi.create.foundation.transfer.fluid.\1/g' \
  -e 's/net\.neoforged\.neoforge\.capabilities\./com.simibubi.create.foundation.transfer.capability./g' \
  -e 's/net\.neoforged\.neoforge\.common\.util\.INBTSerializable/com.simibubi.create.foundation.transfer.util.INBTSerializable/g'

# NeoForge extension methods Level#getCapability / ItemStack#getCapability -> Fabric-style lookup.find(...)
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/([A-Za-z_][A-Za-z0-9_.]*(\(\))?)\.getCapability\((Capabilities\.[A-Za-z]+\.BLOCK),\s*/\3.find(\1, /g' \
  -e 's/([A-Za-z_][A-Za-z0-9_.]*(\(\))?)\.getCapability\((Capabilities\.FluidHandler\.ITEM)\)/\3.find(\1, null)/g'

# ---- misc 1:1 class swaps ----
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.common\.Tags\b/com.simibubi.create.foundation.tags.Tags/g' \
  -e 's/net\.neoforged\.neoforge\.common\.util\.FakePlayer\b/net.fabricmc.fabric.api.entity.FakePlayer/g' \
  -e 's/net\.neoforged\.neoforge\.common\.util\.TriState\b/net.fabricmc.fabric.api.util.TriState/g'

# NeoForge's static DyeColor#getColor(ItemStack) patch -> Create's DyeHelper (statics can't be interface-injected)
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/\bDyeColor\.getColor\(/com.simibubi.create.foundation.utility.DyeHelper.getColor(/g'

# ---- events -> Create-owned event bridge (com.simibubi.create.foundation.event.*) ----
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e '/^import net\.neoforged\.neoforge\.common\.NeoForge;$/d' \
  -e 's/\bNeoForge\.EVENT_BUS\b/com.simibubi.create.foundation.event.bus.CreateEventBus.BUS/g' \
  -e 's/net\.neoforged\.bus\.api\./com.simibubi.create.foundation.event.bus./g' \
  -e 's/net\.neoforged\.fml\.common\.EventBusSubscriber/com.simibubi.create.foundation.event.bus.EventBusSubscriber/g' \
  -e 's/net\.neoforged\.fml\.event\.lifecycle\./com.simibubi.create.foundation.event.lifecycle./g' \
  -e 's/net\.neoforged\.fml\.event\.config\./com.simibubi.create.foundation.event.lifecycle./g' \
  -e 's/net\.neoforged\.neoforge\.client\.event\./com.simibubi.create.foundation.event.client./g' \
  -e 's/net\.neoforged\.neoforge\.event\./com.simibubi.create.foundation.event./g'
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.common\.util\.BlockSnapshot\b/com.simibubi.create.foundation.event.BlockSnapshot/g'

# ---- datagen model DSL -> no-op shell (com.simibubi.create.foundation.data.modelgen) ----
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.client\.model\.generators\./com.simibubi.create.foundation.data.modelgen./g' \
  -e 's/net\.neoforged\.neoforge\.common\.data\.ExistingFileHelper\b/com.simibubi.create.foundation.data.modelgen.ExistingFileHelper/g'

# ---- client model data / extensions -> Create-owned FRAPI-bridged classes ----
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.client\.model\.data\./com.simibubi.create.foundation.model.data./g' \
  -e 's/net\.neoforged\.neoforge\.client\.model\.(BakedModelWrapper|IDynamicBakedModel|QuadTransformers)\b/com.simibubi.create.foundation.model.\1/g' \
  -e 's/net\.neoforged\.neoforge\.client\.ChunkRenderTypeSet\b/com.simibubi.create.foundation.model.ChunkRenderTypeSet/g' \
  -e 's/net\.neoforged\.neoforge\.client\.extensions\.common\./com.simibubi.create.foundation.client.extensions./g' \
  -e 's/net\.neoforged\.neoforge\.client\.extensions\.IGuiGraphicsExtension\b/com.simibubi.create.foundation.client.extensions.IGuiGraphicsExtension/g'

# ---- registries -> Create-owned DeferredRegister/DeferredHolder over vanilla Registry.register ----
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.registries\.(DeferredRegister|DeferredHolder)\b/com.simibubi.create.foundation.registry.\1/g'
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.registries\.RegisterEvent\b/com.simibubi.create.foundation.registry.RegisterEvent/g'

# ---- CC:Tweaked NeoForge capability -> Fabric lookup (same BlockApiLookup shape) ----
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/dan200\.computercraft\.api\.peripheral\.PeripheralCapability\b/dan200.computercraft.api.peripheral.PeripheralLookup/g' \
  -e 's/\bPeripheralCapability\.get\(\)/PeripheralLookup.get()/g'

# ---- fluids: FluidType/BaseFlowingFluid/FluidInteractionRegistry/NeoForgeMod -> Create-owned fluid layer ----
find "$target" -name '*.java' -print0 | xargs -0 sed -i -E \
  -e 's/net\.neoforged\.neoforge\.fluids\.(FluidType|BaseFlowingFluid|FluidInteractionRegistry)\b/com.simibubi.create.foundation.transfer.fluid.\1/g' \
  -e 's/net\.neoforged\.neoforge\.common\.(SoundActions|SoundAction)\b/com.simibubi.create.foundation.transfer.fluid.\1/g' \
  -e 's/net\.neoforged\.neoforge\.common\.NeoForgeMod\b/com.simibubi.create.foundation.transfer.fluid.CommonFluids/g' \
  -e 's/\bNeoForgeMod\./CommonFluids./g'
