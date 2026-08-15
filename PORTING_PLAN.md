# Create → Fabric 1.21.1 Porting Plan

Source of truth: `/home/ledok/Projects/Mods/Create` (NeoForge, branch `mc1.21.1/dev`, mod_version 6.0.11).
Target: this repo, package `com.simibubi.create` (kept identical to upstream so future updates can be
diffed/reapplied instead of re-translated from scratch).

Strategy chosen: **fresh native port** — no Sinytra Connector / Porting-Lib shim. Every NeoForge API call
gets replaced with a real Fabric equivalent. This is the slow, correct path; expect this to span many
sessions. Update the checkboxes below as phases land so any future session can pick up state without
re-discovering it.

## Ecosystem findings (Aug 2026)

- No complete Create-on-Fabric-1.21.1 exists anywhere. `Create-Refabricated` (Fabricators-of-Create's
  official continuation) is stalled at 1.20.1 and incomplete by their own admission. An unofficial fork,
  `vlad250906/Create-UfoPort`, targets 1.21 via Porting-Lib/Forgified-Fabric-API shims — useful as a
  reference for *dependency coordinates*, not as source to copy (different strategy).
- Create's own support libraries **do** have current official Fabric 1.21.1 builds on `maven.createmod.net`,
  which removes what would otherwise be the biggest blockers:
  - `dev.engine-room.flywheel:flywheel-fabric-1.21.1:1.0.6-44` (+ `flywheel-fabric-api-1.21.1`) — rendering engine
  - `net.createmod.catnip:Catnip-Fabric-1.21.1:0.8.54` — Create's shared utility lib
  - `net.createmod.ponder:Ponder-Fabric-1.21.1:1.0.69` — in-world documentation engine
- **Registrate has no Fabric build for 1.21.** `PepperCode1/Registrate-Fabric` and
  `Fabricators-of-Create/Registrate-Refabricated` both stopped at old versions (the latter's newest branch is
  `fabric/1.19`). This is the one real infrastructure gap and blocks almost everything else — see Phase 0b.
- **`Catnip-Fabric-1.21.1` (official, on maven.createmod.net) turned out to be unusable as-is.** It hasn't
  been updated since Dec 2024 and its POM requires `io.github.fabricators_of_create:Porting-Lib` (a
  NeoForge-API compatibility shim, the same approach `Create-UfoPort` uses) plus a forked
  `flywheel-fabric-api:1.0.0-beta-fork.10`. Pulling it in would drag Porting-Lib into the whole project and
  pin Flywheel to that old fork — directly against the native-port strategy. **Decision: Catnip is not taken
  as a prebuilt dependency; it gets ported natively from source, same as Create itself.** See Phase 0a.
- `Ponder-Fabric-1.21.1` (also official, updated as recently as Jun 2026) is fine to depend on directly — its
  only shim-flavored transitive dependency is `forgeconfigapiport-fabric`, which is a real Fabric
  reimplementation of Forge's config format (not a NeoForge-call shim), and is itself a reasonable answer to
  Phase 1's "config system" question. Its transitive `flywheel-fabric-api:1.0.0-9` is excluded in
  `build.gradle` since `flywheel-fabric-<mc>` already bundles those classes (having both caused
  "duplicate input class" jar-merge failures).
- Local reference clones (user-provided, not part of this repo) live in `/home/ledok/Projects/Mods/create-porting/`:
  `Create-UfoPort`, `Registrate`, `Registrate-Refabricated`. Consult these instead of re-deriving facts about
  their APIs via web search.

## Phases

- [x] **Phase 0 — Build skeleton.** Loom + Fabric API + Flywheel/Ponder Fabric deps wired into
      `build.gradle`/`gradle.properties`. Mod bootstrap moved to `com.simibubi.create.Create` /
      `com.simibubi.create.CreateClient` (logging only, no registration yet). Mixin configs repointed.
      `./gradlew build` succeeds. (Catnip dropped as a prebuilt dep — see ecosystem findings above.)
- [x] **Phase 0a — Catnip, vendored from source.** `net.createmod.catnip.*` (187 files) copied from
      `Creators-of-Create/Ponder` (`common/src/main/java/net/createmod/catnip` +
      `fabric/src/main/java/net/createmod/catnip`, commit `8981929` / 2026-06-20 — that repo's build
      publishes both `Ponder-Fabric` and the (stale) `Catnip-Fabric` artifacts from this same living source,
      it just hasn't republished Catnip's own jar/POM since Dec 2024; the source itself is current) into
      `src/main/java/net/createmod/catnip` + `src/main/resources/META-INF/services/net.createmod.catnip.*`
      here. Compiles clean (`./gradlew build` green) against our flywheel 1.0.6-44 + Ponder-Fabric deps, plus
      two added direct deps (`javax.annotation-api`, `jsr305` — upstream only declares these
      non-transitively). `CatnipServices` logs via `net.createmod.ponder.Ponder.LOGGER`, which we get for
      free from the `Ponder-Fabric` dependency already in `build.gradle` — no extra vendoring needed for
      that coupling. **To pull upstream Catnip updates later:** diff against a fresh clone of that repo's
      `mc1.21.1/dev` branch, same two directories.
- [ ] **Phase 0b — Registration layer (blocking).** Decide + build: fork & upgrade Registrate-Refabricated
      to 1.21.1, *or* write a purpose-built Fabric registration helper covering only the builder surface
      `CreateRegistrate`/`AllXxx` classes actually use (`block()`, `item()`, `fluid()`, `blockEntity()`,
      `entity()`, `recipe()`, `tag()`, default creative tab, tooltip modifier hooks). ~30+ `AllXxx.java`
      registry classes depend on this — nothing in Phase 2+ compiles without it.
- [ ] **Phase 1 — Core infra.** `foundation/data`, `foundation/utility`, `foundation/block`,
      `foundation/blockEntity` base classes; config system (NeoForge config → Fabric equivalent);
      networking (`AllPackets`, NeoForge channels → Fabric Networking API payloads); attachments (NeoForge
      `AttachmentType` has no Fabric equivalent — needs a custom or library solution).
- [ ] **Phase 2 — Registry data classes.** `AllBlocks`, `AllItems`, `AllFluids`, `AllBlockEntityTypes`,
      `AllEntityTypes`, `AllRecipeTypes`, `AllSoundEvents`, `AllParticleTypes`, `AllCreativeModeTabs`,
      `AllTags`, `AllDataComponents`, etc. Mostly mechanical once Phase 0b/1 land.
- [ ] **Phase 3 — Kinetics.** Shafts, cogwheels, stress system, Flywheel-backed kinetic rendering. The heart
      of the mod.
- [ ] **Phase 4 — Contraptions.** Movement, assembly, collision. Most complex subsystem; deep NeoForge
      entity/physics hooks to replace.
- [ ] **Phase 5 — Processing.** Mixing, pressing, crushing, fans, spouts, deployers, recipes.
- [ ] **Phase 6 — Logistics.** Packagers, redstone links, display links.
- [ ] **Phase 7 — Trains & tracks.**
- [ ] **Phase 8 — Decoration & palettes.** Mostly plain blocks — good throughput phase.
- [ ] **Phase 9 — Equipment.** Tools, armor, potato cannon.
- [ ] **Phase 10 — Ponder integration.** Engine dependency is already wired (Phase 0); this phase wires up
      Create's actual ponder scene registrations.
- [ ] **Phase 11 — Datagen.** NeoForge datagen → Fabric data generation API (recipes, loot tables, tags,
      models).
- [ ] **Phase 12 — Compat modules.** JEI, Curios, CC: Tweaked, Farmer's Delight, Sodium. Lowest priority,
      mostly optional, do last.
- [ ] **Phase 13 — Polish.** Sounds, lang, advancements, worldgen, mixin audit.

## Licensing note

Upstream's code (`src/main/java/**`) is MIT — safe to port and republish. Its **assets**
(`src/main/resources/assets/**`: textures, models, sounds, lang) are **All Rights Reserved**, copyright The
Create Team. Copying them here is fine for local/personal testing, but this repo should not publicly
distribute Create's original assets without permission — Phase 2+ content work should keep that boundary in
mind (e.g. placeholder/own-authored assets for anything meant to be shared, or documenting that end users
must supply their own).

## Conventions

- Package names mirror upstream exactly (`com.simibubi.create.*`) — do not rename packages while porting.
- Single unified source set (no `loom.splitEnvironmentSourceSets()`). Client-only code is gated with
  `@OnlyIn(Dist.CLIENT)`/`@Environment(EnvType.CLIENT)` like upstream, not physically separated — sorting a
  codebase this size into client/main source sets by hand isn't tractable. Everything lives under
  `src/main/java`.
- When a piece of upstream's own infrastructure (Catnip, Ponder, Flywheel) already has current, real Fabric
  source available (as opposed to NeoForge source needing translation), prefer vendoring/depending on that
  directly over re-deriving it — see Phase 0a for the pattern.
- When a NeoForge API has no Fabric equivalent, note the replacement decision in this file under the
  relevant phase before writing code that depends on it, so it isn't re-litigated next session.
