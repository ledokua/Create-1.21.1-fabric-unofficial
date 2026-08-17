# Changelog

## Unreleased

**Fixed**
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
