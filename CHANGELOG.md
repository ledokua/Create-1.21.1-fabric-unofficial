# Changelog

## 6.0.11-fabric.10

**Fixed**
- Moving contraptions (gantries, trains, pistons, ...) were invisible with Sodium when they carried copycats, belts,
  tracks, brass tunnels or water wheels, until F3+A/F3+T. Flywheel sets up a contraption's visual on a worker thread;
  those block entities asked the port to re-mesh their block, and the port passed that to the real world renderer
  (wrong level, wrong thread). Sodium throws there, which aborted the visual. Re-mesh requests now only reach the
  world renderer for the client's own level, on the render thread.

## 6.0.11-fabric.9

**Fixed**
- Train controls turned on and straight back off on right-click, so trains couldn't be driven. Any right-click Create
  handles itself (contraption controls, doors and levers on contraptions, super glue, track curves) fired two or three
  times per click: the port's hook cancelled vanilla's use action before it set its 4-tick repeat delay, so vanilla's
  "use key held" check fired it again in the same tick. A held key now repeats every 4 ticks, as in vanilla/NeoForge.
- Looking up a potion fluid's recipes in EMI listed every automatic brewing recipe (hundreds of pages). EMI now tells
  potion fluids apart by potion, as JEI already did.
- The threshold switch treated a drawer slot (Extended Drawers and other Fabric bulk storages) as one stack, so its max
  was far too low. It now uses the slot's real capacity. Vanilla containers are unchanged.
- Elevator contraption controls logged a "ThreadLocalRandom accessed from a different thread" stack trace every frame
  with C2ME installed.

**Performance**
- Filters with recipe attributes ("can be smelted", "can be smoked/blasted/crushed/washed/haunted") ran a recipe lookup
  for every item tested, so a funnel or belt funnel with such a filter on a busy inventory searched the recipes for
  every slot, every tick. The answer is now remembered per item until recipes reload.

**Changed**
- Drills and saws no longer break Budding Amethyst (moving ones stop at it, as at any unbreakable block), so they can
  harvest the clusters around it. Server config `kinetics.drillsBreakBuddingAmethyst` (default false) restores
  breaking it.

## 6.0.11-fabric.8

**Added**
- Open Parties and Claims protection for Create. OPAC ships its Create support in its Fabric jar too but turns it on only
  on NeoForge (there was no Fabric Create for 1.21), so on Fabric nothing stopped Create inside claims. With OPAC
  installed, this port now registers OPAC's Create mixins (contraptions picking up or placing blocks, including saved
  minecart contraptions put back into someone's claim; drills, saws, harvesters, ploughs and deployers; mechanical arms;
  the schematicannon; the symmetry wand; minecart couplings) and redoes OPAC's NeoForge-only part for this port (claim
  checks on Create's packets: block entity settings, toolboxes, clipboards, super glue, the wrench menu, contraption
  and train controls, elevators, train relocation; open pipe ends; potato cannon hits). `-Dcreate.opacCompat=false`
  turns it off.

## 6.0.11-fabric.7

**Performance**
- Chutes, funnels and everything else that reads other mods' inventories were very slow against big storages
  (storage networks such as Tom's Storage, large chest walls). For storages without numbered slots, every slot lookup
  re-walked the whole storage, so one failed chute insert into a full 2700-slot network took ~150 ms, every tick.
  Slot lists are now built once per tick; inserts go to the storage in one call (~0.04 ms); and chutes and funnels
  facing such a storage (128+ slots) retry a failed transfer after 2, 4, 8, 16, then 20 ticks instead of every tick.

## 6.0.11-fabric.6

**Fixed**
- The schematic tool menu stayed focused after releasing the Tool Menu key, so the tool couldn't be changed. Create
  never saw any key release: its key event hooked only the last return of vanilla's key handler, and releases leave
  through an earlier one.

## 6.0.11-fabric.5

**Fixed**
- With Sodium, the machines in recipe viewer animations (EMI/JEI: press, basin, mixer, deployer, ...) were drawn
  partly or fully black: fully black with Flywheel's backend off, e.g. under Iris shaders. Sodium lights those blocks
  itself and treats the inside of a solid block as dark. Block models drawn in GUIs now always get full-bright light,
  as they already did with Fabric's own renderer.

## 6.0.11-fabric.4

**Fixed**
- Sneak or sprint stopped working when bound to the same key as one of Create's keys, e.g. sneak on Left Ctrl.
  Vanilla gives each physical key to only one key mapping. Create's Shift/Ctrl/Alt modifiers and its hidden
  schematic-tool key (Left Ctrl) were plain mappings there and could take the key. With the defaults, that could
  already cost sprint its Left Ctrl. They now stay out of vanilla's key lookup and are read from the key's state, as
  they effectively are on NeoForge. They can still be rebound in Controls, and are no longer shown as conflicts.
- Holding the toolbelt (or wrench menu) key no longer depends on which mapping owns that key: toolmenu and toolbelt
  share Left Alt (as upstream), and only one of them ever saw it as held.

## 6.0.11-fabric.3

**Fixed**
- Client crash `ShadedBlockSbbBuilder only supports putBulkData!` when a moving contraption (or a schematic preview)
  holding one of Create's data-aware blocks (casings with connected textures, copycats, encased shafts, ...) was drawn
  while Flywheel's backend is off, which it switches to under Iris shaders. Fabric's renderers (Sodium, Indigo) write
  those blocks vertex by vertex, which Catnip's buffer builder rejected; the vertices now go into its underlying
  buffer, with the per-quad shading kept.

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
