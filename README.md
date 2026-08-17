<h1 align="center">Create — unofficial Fabric port</h1>

<p align="center">
A full port of <a href="https://github.com/Creators-of-Create/Create">Create</a> <b>6.0.11</b> (NeoForge, MC 1.21.1)
to <b>Fabric 1.21.1</b>, written directly against the Fabric API — no Sinytra Connector, no Porting Lib.
</p>

> **This is not an official Create release.** It is not affiliated with the Create Mod Team or with
> [Fabricators-of-Create](https://github.com/Fabricators-of-Create/Create), who maintain the official Fabric port.
> Report problems with this build here, never to upstream Create.

## What it is

Create's full feature set on Fabric: kinetics, contraptions, trains, processing, logistics/packages, schematics,
decoration & palettes, equipment, and Ponder — all from upstream 6.0.11's source, with NeoForge-specific
infrastructure reimplemented on Fabric APIs.

Everything is the mod id `create`, so Create addons that only use Create's own classes work unchanged.

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | Fabric Loader 0.19.3+ |
| Required | Fabric API, [Ponder](https://modrinth.com/mod/ponder), Forge Config API Port |
| Strongly recommended | [Flywheel](https://modrinth.com/mod/flywheel) (client-side rendering backend; a no-op server stub is bundled so dedicated servers run without it) |
| Optional compat | JEI, EMI, Mod Menu, Sodium, Trinkets, CC: Tweaked, Farmer's Delight, Xaero's World Map, JourneyMap, FTB Chunks |

## Recipe viewers

The mod ships a **native EMI plugin** and a JEI plugin, both built on one shared, viewer-neutral category layer.
All 23 Create categories (milling, crushing, mixing, sequenced assembly, …) show up with their animations,
workstations, ghost-ingredient drag & drop, and "fill" buttons for the crafting blueprint and the stock keeper.
If both viewers are installed, the JEI plugin stands down so recipes are not listed twice.

## Differences from the official Fabric port

- No Porting Lib / Registrate-Refabricated dependency. Registrate is reimplemented in-tree
  (`com.tterrag.registrate`), and NeoForge's event bus, capabilities, model loaders, data maps and client hooks
  are reimplemented under `com.simibubi.create.foundation` (~168 mixins, no Forge shim layer).
- Tracks upstream **6.0.11**.
- Ships NeoForge-style `c:` item tags that Fabric's conventions lack (`c:slimeballs`, `c:eggs`, …) so recipes
  that upstream mods write against Create's NeoForge tags resolve.
- Accepts legacy Create-Fabric 0.5.x fluid JSON in recipes, so recipes written for the old Fabric port still load.

## Building

```bash
./gradlew build          # -> build/libs/Create-<mod_version>.jar
./gradlew runGameTest    # upstream's 71 gametests (not part of `build`)
./gradlew runClient      # dev client; -PwithEmi / -PwithSodium / -PwithTrinkets / -PwithFtbChunks add optional mods
```

Resources are vendored from upstream and patched for Fabric by `scripts/convert-resources.py`;
`scripts/check-mixin-targets.py` verifies every mixin target still exists.
`PORTING_PLAN.md` documents the port's architecture, the traps found along the way, and what is still open.

## Status

Runs on client and dedicated server, joins multiplayer, and passes upstream's 71 gametests. It has been played
on a 398-mod server pack. Rendering is checked with an automated screenshot harness
(`CREATE_HARNESS=showcase ./gradlew runClient`). Known open items are listed in `PORTING_PLAN.md`.

## Credits & licence

Create is by **simibubi** and the Create Mod Team; this port only adapts their work. Ponder, Flywheel, Catnip and
Registrate are by their respective authors. Distributed under the same licence as upstream Create — see
[LICENSE.txt](LICENSE.txt). Fabric port by **LeDok**.
