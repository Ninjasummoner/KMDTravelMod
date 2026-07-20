<div align="center">
  <img src="https://ninjasummoner.github.io/KMDTravelMod/assets/images/KMD_Travel.png" alt="KMD Travel" />
</div>

<p align="center">
  <a href="https://www.modpackindex.com/mod/160658/kmd-fast-travel">
    <img src="https://www.modpackindex.com/badge/mod/160658/kmd-fast-travel/version.svg" alt="Supported Minecraft versions" />
  </a>
  <a href="https://www.modpackindex.com/mod/160658/kmd-fast-travel">
    <img src="https://www.modpackindex.com/badge/mod/160658/kmd-fast-travel/updated.svg" alt="Last updated" />
  </a>
</p>

# KMD Travel

KMD Travel brings risk/reward fast travel to Minecraft. Place travel posts, discover routes, open a parchment-style map, and deal with events that can interrupt the journey.

## Features

- Fast travel posts for discovered routes between locations.
- Shared travel posts that can be visible to everyone on a server.
- Parchment map UI with discovered areas, pins, labels, ambush chance, zoom, and panning.
- Route interruptions with passive events, hostile encounters, skip chance, and configurable ambush risk.
- Custom event profiles with literal summon-style NBT, mobs, commands, delays, dimensions, biomes, weights, and avoid chance.
- World/server-specific event profiles and dimension-aware map cache data.
- Server-friendly configuration for event chances, armor/hunger/distance modifiers, passive timers, and shared-post crafting.
- Separate Forge, NeoForge, and Fabric projects so each loader can be tested and released independently.

## Supported Builds

| Loader | Minecraft | Java | Folder |
| --- | --- | --- | --- |
| Forge | 1.20.1 | 17 | `Forge/KMD Travel - Forge 1.20.1` |
| NeoForge | 1.21.1 | 21 | root project |
| NeoForge | 1.21.10 | 21 | `NeoForge/KMD Travel - NeoForge 1.21.10` |
| Fabric | 1.21.1 | 21 | `fabric` |
| Fabric | 1.21.10 | 21 | `Fabric Versions/KMD Travel - Fabric 1.21.10` |
| Fabric | 26.2 | 25 | `Fabric Versions/KMD Travel - Fabric 26.2` |

## Quick Start

1. Install the correct KMD Travel jar for your loader and Minecraft version.
2. Place a travel post in the world.
3. Right-click it to discover it.
4. Place and discover another travel post.
5. Right-click a discovered post to open the travel map and choose a destination.

The craftable **Travel Map** provides a portable alternative. Selecting it plays the scroll-opening animation and renders the live explored map between the player's hands. Right-clicking it opens the same destination UI as a travel post, centered on the player.

Travel Map recipe:

```text
Ink Sac       | Empty | Feather
Leather       | Paper | Leather
Ender Pearl   | Empty | Ender Pearl
```

Sneak-right-click a travel post to rename it. Right-click with dye to change sign text color. Right-click with a banner to change that post's map marker.

## Commands

Use `/kmdtravel help` in-game for the full command guide.

Common admin commands:

```mcfunction
/kmdtravel events true
/kmdtravel eventwait 60
/kmdtravel basechance 0.25
/kmdtravel maxchance 0.75
/kmdtravel sharedpostcrafting false
/kmdtravel eventprofiles
/kmdtravel finishencounter @p
```

## Event Profiles

Custom event profiles are world/server-specific. A server owner can create profiles in-game through `/kmdtravel eventprofiles`, then edit event settings, mobs, commands, delays, dimensions, biomes, and selection weights.

Profiles can also be exported as JSON for easier editing and sharing. Template profiles placed in the modpack-level `kmdtravel/events` folder are copied into new worlds. KMD also ships the `KMD_Events` profile as a baseline profile; if that bundled profile is untouched, updates can refresh it safely without overwriting custom profiles.

The mob NBT field accepts the same compound used after an entity ID in `/summon`. KMD preserves complete compounds instead of rewriting equipment, attributes, trades, or component data.

See [Default Events](events-reference.md) for the shipped event list and version-specific NBT notes.

## Configuration And Generated Files

KMD keeps modpack-level templates separate from world-specific data.

Typical generated folders:

```text
.minecraft/
`-- kmdtravel/
    |-- events/              # templates plus active world/server subfolders
    |   `-- <world-name>_<id>/
    |-- map-cache/           # client explored-terrain cache, separated by server/world and dimension
    `-- recipes/             # exported editable recipe references

.minecraft/config/kmdtravel/
`-- kmdtravel-common.*       # loader-specific common configuration
```

World profiles remain isolated because each world/server receives its own folder under `kmdtravel/events`. Template profile JSON files placed directly in `kmdtravel/events` are copied into a world folder only when that world is initialized.

## Documentation

- [KMD Wiki](https://ninjasummoner.github.io/KMDTravelMod/)
- [Addon Development](addon-development.md)
- [Default Event Profile](events-reference.md)
- [Version Guide](VERSIONS.md)
- [Contributing](CONTRIBUTING.md)

## Building

Use the Gradle wrapper inside the loader/version folder you want to build.

```powershell
cd "NeoForge/KMD Travel - NeoForge 1.21.10"
./gradlew.bat build
```

```powershell
cd "Fabric Versions/KMD Travel - Fabric 1.21.10"
./gradlew.bat build
```

Forge 1.20.1 requires Java 17. Minecraft 1.21.x loaders require Java 21. The 26.2 Fabric project currently requires Java 25.

## Repository Layout

```text
.
|-- docs/                  # wiki-style documentation
|-- Forge/                 # Forge 1.20.1 project
|-- NeoForge/              # NeoForge projects by Minecraft version
|-- fabric/                # Fabric 1.21.1 project
`-- Fabric Versions/       # Fabric 1.21.10+ projects
```

## Addons And Modpacks

KMD Travel is source-available and addon-friendly. Addons, datapacks, recipe packs, translations, quest integrations, and compatibility mods are allowed when they depend on KMD Travel instead of bundling or republishing KMD Travel code/assets.

You may include official KMD Travel releases in public or private modpacks without asking first.

## License

KMD Travel uses the custom [`KMD Travel Source-Available License`](LICENSE.md). The source is visible for transparency, contribution, addon development, and compatibility work, but KMD Travel itself may not be rehosted, renamed, or redistributed as a standalone fork without permission.

## Credits

Created by **Ninjasummoner**.
