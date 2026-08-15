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
