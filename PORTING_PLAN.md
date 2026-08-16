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
- [x] **Phase 0a — Catnip.** Initially vendored from source (`net.createmod.catnip.*`, 187 files from
      `Creators-of-Create/Ponder` commit `8981929`); **later removed again (Phase 3)**: the official
      `Ponder-Fabric-1.21.1` jar already bundles the compiled Catnip (same source snapshot, no Porting-Lib
      references, incl. `META-INF/services/net.createmod.catnip.*`), so Create now compiles against Ponder's copy
      via `modImplementation Ponder-Fabric`. Shipping our own duplicate would have raced Ponder's classes at
      runtime. (The stale standalone `Catnip-Fabric` maven artifact is still not used.) `CatnipServices` logs via
      `net.createmod.ponder.Ponder.LOGGER`.
- [x] **Phase 0b — Registration layer.** Purpose-built Fabric-native reimplementation of
      `com.tterrag.registrate` (40 files, ~1.6k lines, under `src/main/java/com/tterrag/registrate`), same
      package/class names and public signatures as upstream so ported Create code needs zero import changes.
      Not a fork of Registrate-Refabricated (depends on `Porting-Lib` for datagen) and not a mechanical port of
      upstream (bound end-to-end to NeoForge's `DeferredHolder`/`RegisterEvent`).
      **Verified at runtime** on a dedicated server via a throwaway smoke test (since deleted): block +
      nested `.item()` + block entity declared *before* its block with a lazy `validBlock` reference +
      extended-data menu; `onRegister`/`onRegisterAfter(ITEM)` fired in the right order; client-only hooks
      correctly skipped server-side; no dangling-callback warnings.

      **Design, decided from a 3-way research pass (upstream Registrate's `1.21.1/dev` branch,
      Registrate-Refabricated's `fabric/1.19` branch, and a grep-survey of every Registrate call site in
      Create's own source):**
      - *Still two-phase, but flushed by an explicit ordered pass instead of an event bus.* Builders file a
        `Registration` on `.register()`; nothing is constructed until `AbstractRegistrate.register()` runs the
        flush in a fixed registry order (SOUND_EVENT, FLUID, BLOCK, ATTRIBUTE, ENTITY_TYPE, ITEM,
        BLOCK_ENTITY_TYPE, MENU, CREATIVE_MODE_TAB, then anything else first-touched order) — the same order
        NeoForge fires `RegisterEvent` in. This is what keeps upstream's call-order-independent code working
        (a `BlockBuilder.item()` factory referencing its block, a BE type listing blocks declared later, …).
        Upstream never used `DeferredRegister`, only `DeferredHolder`+`RegisterEvent`, so nothing of that
        shape needed replacing — Fabric registries are simply open during `onInitialize()`.
        **`Create.onInitialize()` must call `REGISTRATE.register()` once, after all `AllXxx.register()`
        static-inits have run** — that's the flush trigger (upstream gets it for free from the event bus).
      - *`RegistryEntry<R, T>` keeps upstream 1.21's two-type-parameter shape* (Create spells it out at ~50
        sites) but does NOT extend NeoForge's `DeferredHolder`; it's created unresolved and filled in by the
        flush via `resolve(T)`. `RegistryEntry#getKey()`/`#asHolder()` cover the DeferredHolder-isms Create
        uses.
      - *Client hookups are real, not stubbed*: `.renderer()` (BE + entity), `.color()` (block + item),
        `.addLayer()`, menu screens go through `com.tterrag.registrate.fabric.RegistrateClientHooks` (Fabric
        API's `EntityRendererRegistry`/`ColorProviderRegistry`/`BlockRenderLayerMap`, vanilla
        `BlockEntityRenderers`/`MenuScreens`) behind an env check (`RegistrateEnv.runOnClient`), same
        Supplier<Runnable> trick as NeoForge's DistExecutor so servers never load client classes.
        `.attributes()` uses `FabricDefaultAttributeRegistry`.
      - *Extended-data menus* (all 13 of Create's, via upstream's `ForgeMenuFactory` shape) map onto Fabric's
        `ExtendedScreenHandlerType` carrying an opaque `RegistrateMenuData` byte blob, so the client factory
        still receives a `RegistryFriendlyByteBuf` unchanged. **Phase 1 TODO:** Create's server-side
        `player.openMenu(provider, buf -> …)` call sites need a small `MenuProvider` shim implementing
        Fabric's `ExtendedScreenHandlerFactory<RegistrateMenuData>` using `RegistrateMenuData.write(...)`.
      - *No-ops with matching signatures* (accepted-and-dropped, so ported chains compile): `.tag()`,
        `.lang()`/`defaultLang()`, `.blockstate()`/`.model()`/`.loot()`/`.recipe()` and their `default*`
        variants, `addDataGenerator()`, `addLang()`, `defaultCreativeTab()`, `ItemBuilder.tab()`,
        `.burnTime()`/`.compostable()` (NeoForge data maps — need Fabric's fuel/compost registries directly at
        the AllItems call sites in Phase 2). Rationale for the datagen ones is below.
      - *Deferred to Phase 2 with concrete content:* `FluidBuilder` is a bare shell (upstream's is 100%
        NeoForge `FluidType`; Create has only 4 fluids, all via `CreateRegistrate.standardFluid`/
        `virtualFluid` — design the Fabric fluid against those). `EntityBuilder.spawnPlacement()`/spawn eggs
        omitted (unused by Create).
      - *Datagen-hook builder methods (`.blockstate()`, `.model()`, `.loot()`, `.recipe()`, `.lang()` — 322
        call sites total across Create) are signature-compatible no-ops*: upstream Create's repo already
        ships **pre-generated** JSON under `src/generated/resources/`, which Phase 2 vendors directly as
        static resources — nothing needs to actually run datagen yet. **Caveat discovered on inspection: the
        lambda *bodies* at those call sites still have to compile**, and they call ~30 distinct methods on
        the provider objects (`p.simpleBlock`, `p.models().cubeAll`, `p.modLoc`, `p.horizontalBlock`,
        `p.getVariantBuilder`, `p.stonecutting`, `p.dropOther`, …). So the provider classes under
        `com.tterrag.registrate.providers` (currently empty placeholders) will grow no-op-bodied methods
        with upstream's signatures, compile-error-driven, as Phase 2 files land — a loose skeleton Phase 11
        then fills in for real. Don't pre-build NeoForge's whole model-generator DSL speculatively.
      - Coverage prioritized by call-site frequency in Create: `block()`/`item()`/`blockEntity()` (194/50/9
        entry-point sites; `.register()`/`.transform()`/`.properties()`/`.tag()` used 200-400+ times each)
        are fully implemented. `entity()`/`menu()` (2/1 sites) functional. `fluid()` deferred (above).
        Unused upstream surface (enchantment/particle/biome builders, `.removeTag()`, spawn eggs, etc.)
        skipped entirely.
      - Create's own additions on top of base Registrate — `CreateRegistrate` (custom creative-tab lookup,
        tooltip-modifier hook; overrides `accept(...)` and `blockEntity`/`entity` via the protected
        `entry(name, callback -> …)` seam, both kept upstream-shaped for exactly this), `CreateBlockEntityBuilder`/
        `CreateEntityBuilder` (`.visual()` for Flywheel visualizer registration — implement for real via
        `RegistrateEnv.runOnClient`, Flywheel is a live dependency), `SimpleBuilder` (Create's custom-registry
        mechanism for `displaySource`/`displayTarget`/`mountedItemStorage`) — ported in Phase 1
        (`foundation/data`), not as part of Registrate itself. Note `CreateRegistrate` types its creative-tab
        field as NeoForge `DeferredHolder<CreativeModeTab, CreativeModeTab>` — becomes `RegistryEntry` /
        `ResourceKey<CreativeModeTab>` in the port.
## NeoForge → Fabric translation strategy (decided at the start of Phase 1)

Measured on upstream: 701 of ~2,016 files import NeoForge, from only **217 distinct classes**, clustering
into a few buckets. Rule of thumb throughout: **keep upstream's code shape** (so future syncs stay diffs)
and swap the *implementation* underneath, the same way Catnip and Registrate were handled.

| Bucket | ~sites | Translation |
|---|---|---|
| `@OnlyIn(Dist.CLIENT)` / `Dist` | 440 | Mechanical rewrite to `@Environment(EnvType.CLIENT)` (`scripts/` sed pass, re-runnable on sync). |
| **Item/fluid handler API** — `IItemHandler`, `IItemHandlerModifiable`, `ItemStackHandler`, `ItemHandlerHelper`, `SlotItemHandler`, `RecipeWrapper`, `CombinedInvWrapper`, `FluidStack`, `IFluidHandler(Item)`, `FluidTank`, `FluidAction`, `SizedFluidIngredient`… | ~450 | **Create owns these abstractions** — port them as Create classes (upstream shape/semantics, no `net.neoforged` package; e.g. `com.simibubi.create.foundation.item.handler.*`, `…fluid.*`) and mechanically rewrite imports. Only ~55–72 files do actual capability *lookups* (`level.getCapability(Capabilities.ItemHandler.BLOCK, …)`); those go through a Create-owned `BlockApiLookup<IItemHandler, Direction>` (Fabric API's real lookup mechanism) with two-way adapters to Fabric Transfer API (`ItemStorage.SIDED`/`FluidStorage.SIDED` ⇄ `IItemHandler`/`IFluidHandler`) so vanilla containers and other Fabric mods interoperate. **This is the single biggest fork from upstream and the only viable one** — rewriting ~450 sites onto Transfer API's transaction/variant model directly is a multi-year effort that would also permanently diverge from upstream. |
| Events — `@EventBusSubscriber`/`@SubscribeEvent`, `PlayerInteractEvent`, `NeoForge.EVENT_BUS`, `InputEvent`, `EventHooks`… (65 distinct event types, 120 handler methods in 55 files, concentrated in `foundation/events/ClientEvents`+`CommonEvents` and `ExtendoGripItem`) | ~250 | **Create-owned event bridge** (`com.simibubi.create.foundation.event.*`, mirroring NeoForge's event package tree so it's a prefix sed): a minimal event bus, event classes exposing exactly the members Create's handlers use, fired from Fabric callbacks (`UseBlockCallback`, `ServerTickEvents`, `HudRenderCallback`, …) or small mixins where Fabric has no hook. `@SubscribeEvent`/`@EventBusSubscriber` stay as Create-owned marker annotations; registration is reflective over an *explicit* list of subscriber classes (no classpath scanning). Rationale: keeps all 120 handler bodies unchanged and centralizes each event's semantic mapping once, instead of re-deriving it at every site; rewriting sites into Fabric lambdas would permanently diverge from upstream. Handlers subscribed to an event type that isn't wired to a Fabric hook yet are **logged at init** — a self-maintaining worklist, never a silent no-op. Create's own API events (`com.simibubi.create.api.event.*`, which upstream builds on NeoForge's `Event`) extend the bridge's `Event`. |
| NeoForge patch-methods on vanilla classes — `Entity/BlockEntity#getPersistentData()` (70), `BlockEntity#invalidateCapabilities()` (22), `AbstractContainerScreen#getGuiLeft/Top()` (41), `ItemStack#getCraftingRemainingItem()` (11), … | ~150 | Loom **interface injection** (`loom:injected_interfaces` in fabric.mod.json) + a mixin implementing the injected interface on the vanilla class — Fabric's own mechanism for "add a method to a vanilla class". Call sites compile unchanged. `invalidateCapabilities()` is a no-op (Fabric's `BlockApiCache` invalidates on block-entity change by itself). |
| `Tags.Items/Blocks/Fluids` (NeoForge convention tags) | 130 | Create-owned `Tags` class with NeoForge's constant names bound to the same `c:` tag ids Fabric's `ConventionalItemTags` use on 1.21 (they agree on ids, not on constant names) — same code shape, full interop. |
| Datagen model DSL — `net.neoforged.neoforge.client.model.generators.*` (`ModelFile`, `ConfiguredModel`, `VariantBlockStateBuilder`, `models().cubeAll(...)`, `p.modLoc(...)`, …) referenced by the ~320 datagen lambdas + `foundation/data/BlockStateGen` etc. | ~450 | No-op-bodied shell classes with upstream's signatures (Create-owned, `com.simibubi.create.foundation.data.modelgen.*` + the `com.tterrag.registrate.providers.*` placeholders), built compile-error-driven. Bodies never run (Phase 0b decision: vendored pre-generated JSON); Phase 11 replaces the shell with real Fabric datagen. |
| Client model data / model gen — `ModelData`, `ModelProperty`, `IDynamicBakedModel`, `IClientItemExtensions`, `IClientBlockExtensions`; `ModelFile` etc. | ~90 | Runtime ones → FRAPI (`FabricBakedModel.emitBlockQuads`, render-data attachments) in Phase 3. Datagen ones (`ModelFile`, `ExistingFileHelper`) → no-op provider stubs (Phase 0b decision). |
| `Tags.Items/Blocks/Fluids` (NeoForge convention tags) | 26 | `net.fabricmc.fabric.api.tag.convention.v2.Conventional*Tags` — both use the `c:` namespace on 1.21 so ids mostly match. |
| `FakePlayer` | 19 | `net.fabricmc.fabric.api.entity.FakePlayer` (Fabric API). |
| `AttachmentType`/data attachments | ~10 | `fabric-data-attachment-api-v1` — near 1:1. |
| `DeferredRegister`/`DeferredHolder` (`AllRecipeTypes`, `AllCreativeModeTabs`, …) | 24 | Direct `Registry.register` or `REGISTRATE.simple(...)`. |
| Config — `ModConfigSpec`, `ModConfig`, `ModConfigEvent` | ~20 | `forgeconfigapiport-fabric` (already a Ponder dependency; ships these under the same package names — a real Fabric reimplementation of the config format, not a call shim; Catnip's `ConfigBase` already compiles against it here). |
| Networking — `AllPackets` | 1 | Already routed through vendored Catnip's `CatnipPacketRegistry` (Fabric impl present). Free. |
| Fluids — `FluidType`, `BaseFlowingFluid`, `NeoForgeMod.MILK` | ~20 | Phase 2, designed against Create's 4 actual fluids (see Phase 0b). |

**Porting mechanics:** vendor whole directories from upstream in dependency-ordered waves (foundation core →
foundation rest + api → infrastructure → content by subsystem), run the mechanical rewrites, then fix
`compileJava` errors bucket-by-bucket (javac `-Xmaxerrs` raised so the full picture is visible). Files that
drag in unported subsystems get their imports satisfied by porting the dependency, not by stubbing it —
stubs are only used for datagen (Phase 0b decision).

## >>> RESUME HERE (Phase 3 well underway: gametests green, JEI loads, OBJ models render; next = play-testing + Phase 11/12) <<<

State: `./gradlew runServer` boots; `./gradlew runClient` boots and plays; **`./gradlew runGameTest` runs upstream's 65
gametests and they all pass** (contraptions, fluids, items, processing, misc, regressions). JEI (dev runtime) loads Create's
plugin cleanly; Mod Menu shows the config screen entrypoint. Everything is committed.

Infrastructure notes learned while booting (keep in mind for new code):
- **Client classes in common code**: Fabric's dev launcher refuses to load client classes on the server. Lambdas
  inside `@Environment(CLIENT)` methods are NOT stripped by Fabric → `buildSrc/EnvironmentLambdaStripper` (runs in
  `compileJava.doLast`) propagates the annotation to lambdas referenced only from annotated methods. Anything else
  that references client types (e.g. passing `Minecraft.player` (LocalPlayer) to a `Player` param, `BakedModel`
  return-type assignability) must live in a client-only class (`CreateRegistrateClient` pattern).
- `ExtensionOverrides.overrides(...)` (which hook mixins use to only route overriding blocks/items through the
  NeoForge-shaped methods) scans class bytecode with ASM — reflection (`getMethod`) resolves every signature and
  explodes on the server for classes with `@Environment(CLIENT)` methods.
- **Event bus** scans `@SubscribeEvent` via ASM (`EventBus.scanHandlers`) instead of `getDeclaredMethods()`.
- Classes in the mixin package (`foundation.mixin.*`) can't be referenced from normal code — extension interfaces
  live in `foundation.extension`, duck interfaces (`DropCapture`) in `foundation.event`.
- Vanilla lambdas have intermediary names (`method_XXXXX`) even in the mojmap dev env — target them with
  `remap = false` (see `ShapedRecipePatternDataMixin`, `BlockEntityDataPacketMixin`, `LevelChunkUpdateTagMixin`).
- Interface default methods (e.g. `SignalGetter#getSignal`) need an *interface* mixin (`LevelSignalHooksMixin`).
- Fabric API mixin conflicts: `BlockEntityExtensionMixin#getRenderData` needs `@Overwrite` + priority 2000.
- `fabric.mod.json` is token-replaced (not Groovy-expanded) because the injected-interfaces block contains `$`.
- Registrate: re-registering the same name overwrites (upstream semantics; WindowGen relies on it).
- **`scripts/check-mixin-targets.py`**: static javap-based check that every mixin's `method=` overload exists and every
  `@At(INVOKE/FIELD target=…)` occurs in that overload's bytecode. Run it after touching mixins — it catches almost
  everything the game would report as "Mixin apply failed" without booting.
- NeoForge patched-vanilla behaviour that lives in NeoForge overrides of *vanilla* classes (ComparatorBlock#onNeighborChange,
  RedStoneWireBlock#getWeakChanges, RedStoneWireBlock/Repeater/Observer canConnectRedstone) has to be replicated in the
  extension-interface *defaults*, because our hook mixins only route through the extension for classes that override it.
- Reference jars in the loom cache: mojmap vanilla `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-loom.mappings.1_21_1.layered+hash.2198-v2/*.jar`
  and a **NeoForge-patched** mojmap jar `~/.gradle/caches/fabric-loom/1.21.1/neoforge/21.1.209/minecraft-merged-mojang-patched.jar`
  — `javap -p -c` on the latter shows exactly where NeoForge calls each hook.

Done in Phase 3 so far (all runtime-tested by boot + gametests, client-side ones by boot only):
- Hook mixins (`foundation/mixin/hooks/*`, client ones in `hooks/client/*`): piston push reaction, face hiding, weak power,
  friction, step/place sound type, light emission (+ AuxiliaryLightManager for copycat glow), flammability, max stack size,
  onEntitySwing, onEntityItemUpdate, custom item entities, supportsEnchantment, canConnectRedstone, path type, scaffolding,
  explosion resistance, canHarvestBlock/onDestroyedByPlayer, enchant power bonus, onNeighborChange, landing/running effects,
  isBookEnchantable, pick block (getCloneItemStack 5-arg / getPickedResult), reequip animation, BakedModel#applyTransform,
  doesSneakBypassUse (both sides), onItemUseFirst (via UseBlockCallback), item-provided enchantments (diving helmet),
  item decorators, hit/destroy particle effects, helmet overlay, minecart onMinecartPass/canMakeSlopes, ProjectileUtil
  contraption hits, StructureTemplate processEntity, FRAPI AO material.
- Client commands (`foundation/client/command/ClientCommandHandler`, NeoForge semantics; `/create util …`, `/c`).
- Networking: ComplexSpawnPayload wired, data-map client sync (`DataMapSyncPayload`).
- Registry aliases (RemapHelper via Fabric `addAlias`, incl. neoforge:/minecraft: milk → create:milk), ModMenu + JEI entrypoints,
  `c:milk` fluid tag (scripts/extra-resources), capabilities: vanilla Container BEs → InvWrapper/SidedInvWrapper.
- **Model loaders** (`foundation/model/loader/*`): Create's vendored model JSONs use NeoForge's `"loader": "neoforge:obj"`
  (43 models: trains/bogeys, water wheels, flywheel, chain conveyor, blaze burner, crushing wheel, valve handle) and
  `neoforge:composite` (7). Reimplemented on Fabric's model-loading API (`CreateModelLoaders`, `ObjModel`, `CompositeModel`,
  `LoaderBakedModel`), incl. NeoForge's geometry inheritance (a plain child model of an obj model bakes the obj with itself
  as texture owner — `BlockModelGeometryMixin`). Before this those blocks were invisible in inventory / partly unrendered.
- **Screenshot harness** (`infrastructure/debug/DevScreenshotHarness`): `CREATE_HARNESS=showcase ./gradlew runClient
  --args="--quickPlaySingleplayer 'New World (1)'"` builds a showcase of Create blocks in the loaded world, saves
  `run/screenshots/create-harness-{overview,goggles,ponder}.png` and quits — the only automated way to eyeball rendering
  (no client gametest API for 1.21.1). Extend the showcase when touching rendering. Options: `CREATE_HARNESS_PONDER=<item id>`
  picks the Ponder scene; shots: overview, goggles, contraption (spinning bearing rig), creative-base/palettes (creative
  tab selection still lands on vanilla's first page — TODO switch pages), ponder-1..4.
- **Virtual model data**: on Fabric only Flywheel's `VirtualBlockGetter` (SuperByteBuffer baking) counts as "virtual"
  (`FabricModelBridge.isVirtualLevel`); Ponder levels/contraption worlds must not, or bracketed kinetic blocks draw twice.
  User reported "moving parts doubled in Ponder" on the build *before* this fix — re-check with them; if it persists, find
  which block (harness `CREATE_HARNESS_PONDER=`) and compare static-section vs BE-renderer output.
- Sodium compat (reflective SpriteUtil), Catnip now comes from the Ponder-Fabric jar (vendored copy removed).

Next (roughly by user impact):
1. **Human play-test** of client-side systems the gametests can't see: ponder scenes, goggles overlay, schematics/schematicannon,
   trains + track placement, JEI transfer handlers, all GUIs, copycats/CT rendering, packages/factory gauges, item models
   (wrench/zapper/potato cannon), cardboard armor, sound/particles. Log anything odd and fix.
2. Client-side hooks not yet verified in game: BlockDestructionProgress extra positions, RenderHighlight, tooltips (Font ctx),
   fog/camera events, GameRenderer hooks.
3. Datagen shell (Phase 11) — needed only to regenerate resources on Fabric; runtime uses vendored upstream data.
4. Compat modules (Phase 12): CC:Tweaked, Farmer's Delight stub, train map mods (FTB Chunks / Journey / Xaero stubs),
   Sodium/Iris/FTB library, `PotionMixingRecipes` modded brewing is fine (Fabric uses vanilla PotionBrewing.Builder).
5. Publishing: `modLocalRuntime` JEI/ModMenu are dev-only; check the built jar's fabric.mod.json depends block before release.

Useful commands:
```
./gradlew compileJava --console=plain 2>&1 | awk '/^FAILURE/{exit}{print}' > /tmp/c.log; grep -c 'error:' /tmp/c.log
scripts/check-mixin-targets.py            # static mixin target check (run after editing mixins)
scripts/convert-resources.py              # re-vendor upstream resources (wipes assets/ + data/, re-adds EXTRA_FILES)
scripts/list-event-subscribers.sh         # regenerate CreateEventSubscribers.CLASSES
./gradlew runServer --args="nogui"        # run/ is gitignored; eula.txt already accepted
./gradlew runGameTest                     # upstream's 65 gametests (build/run/gameTest); ~1 min incl. gradle
./gradlew runClient --args="--quickPlaySingleplayer 'New World (1)'"   # auto-joins a world (client smoke test)
CREATE_HARNESS=showcase ./gradlew runClient --args="--quickPlaySingleplayer 'New World (1)'"  # + screenshots, then quits
./gradlew runClient -PwithSodium ...      # dev runtime with Sodium (modrinth maven)
```

## Phases (continued)

- [ ] **Phase 1 — Core infra.** `foundation/*` + `api/*` + `impl/*` (~430 files) via the wave process
      above, plus the Create-owned item/fluid handler package described in the table. Also
      `CreateRegistrate`/`CreateBlockEntityBuilder`/`CreateEntityBuilder`/`SimpleBuilder`, the
      `RegistrateMenuData` server-side `openMenu` shim, and wiring `REGISTRATE.register()` into
      `Create.onInitialize()`.
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
