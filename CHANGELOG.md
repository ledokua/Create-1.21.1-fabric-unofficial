# Changelog

## 6.0.11-fabric.2

**Performance**
- The "does this block/item override a NeoForge extension method?" check behind the hook mixins rebuilt a method
  descriptor string (plus a key object and an argument array) on every call, even when the answer was cached. Several
  callers run on hot paths (pathfinding node types for every mob, friction, piston push reaction, entity sound/step
  effects, neighbour updates), where it showed up as ~5–6% of server tick time on a live server. Cached lookups now
  allocate nothing (per-class table, fixed-arity overloads); ~15–20× faster per call, same answers.

**Fixed**
- Andesite/brass funnels could not be placed; display links and super glue failed on interactive blocks. The
  `setUseBlock(FALSE)` bridge now skips only the block's interaction, as on NeoForge.
- Copycat blocks rendered invisible: Fabric keeps one render layer per block, so copycats now take their layers from
  the applied material.
- Custom-rendered items (potato cannon, extendo grip, wand of symmetry, wrench, ...) drew only their static model,
  without the animated parts, because Fabric's item renderer bypassed the custom renderer.
- EMI: `IndexOutOfBoundsException` in the tooltips of sequenced assembly, deploying and item application recipes.
- Crash `IncompatibleClassChangeError: Conflicting default methods: ...ItemInjection.hasCustomEntity /
  ...ItemExtension.hasCustomEntity` whenever an item entity joined the world alongside a mod bundling Porting Lib's
  `entity` module (e.g. Twilight Forest). `Item#hasCustomEntity` / `#createEntity` now have concrete bodies on `Item`
  so both interfaces are satisfied.
- `PackageEntity` had no default attributes registered (upstream's `EntityAttributeCreationEvent` listener was never
  wired), so every dropped package threw an NPE in `LivingEntity#<init>`.

## 6.0.11-fabric.1

First build of the unofficial Fabric port of Create 6.0.11.

**Ported**
- Full mod: kinetics, contraptions, trains & tracks, processing, logistics/packages, schematics, decoration &
  palettes, equipment, Ponder scenes, commands, config, and upstream's gametest suite (71 tests, all passing).
- NeoForge infrastructure reimplemented on Fabric: event bus, capabilities/transfer, attachments, data maps,
  registry aliases, network payloads, model loaders (`neoforge:obj` / `neoforge:composite`), client hooks
  (HUD layers, item decorations, map decorations, tooltips, render stages), and interface injection at runtime.
- Registrate reimplemented in-tree; Catnip/Ponder/Flywheel taken from their official Fabric builds.
- A bundled server-side Flywheel stub, so dedicated servers run without the client-only Flywheel jar.

**Compat**
- Native EMI plugin: all 23 Create categories with animations, workstations, ghost drag & drop, and transfer
  handlers for the crafting blueprint and the stock keeper. JEI plugin defers when EMI is installed.
- Train map overlays for Xaero's World Map, JourneyMap and FTB Chunks.
- Trinkets (goggles in head/face, backtanks in chest/back), Mod Menu, Sodium, CC: Tweaked, Farmer's Delight.

**Fabric-specific fixes**
- Ships NeoForge-style `c:` item tags Fabric lacks, so third-party recipes and EMI's remainder handling resolve.
- Accepts Create-Fabric 0.5.x fluid JSON in recipes.
- `Entity#getPersistentData` is exposed under a uniquely named duck method, avoiding clashes with other mods.

**Known issues**
- Two gametests (`TestItems.storages`, `TestProcessing.precisionMechanismCrafting`) occasionally time out when the
  whole suite runs in one batch; both pass in isolation.
- Iris/shader packs are untested.
