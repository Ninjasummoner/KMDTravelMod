![alt text](https://ninjasummoner.github.io/KMDTravelMod/assets/images/KMD_Travel.png)
 
 # KMD Travel

KMD Travel brings Kingdom Come: Deliverance-style fast travel to Minecraft. Place travel posts, discover routes, and risk interruptions while travelling across your world.

## Features

- **Fast travel posts** for discovered routes between locations.
- **Shared travel posts** that can be visible to everyone on a server.
- **Parchment map UI** with discovered areas, pins, ambush chance, zoom, and panning.
- **Route interruptions** with passive events, hostile encounters, skip chance, and configurable ambush risk.
- **Custom event profiles** with per-world/server profiles, custom mobs, commands, delays, dimensions, biomes, and weights.
- **Map cache** that stores explored terrain per world/server.
- **Server-friendly configuration** for event chances, armor/hunger/distance modifiers, passive timers, and shared-post crafting.
- **Multi-loader support** across Forge, NeoForge, and Fabric builds.

## Supported Builds

| Loader | Minecraft | Folder |
| --- | --- | --- |
| Forge | 1.20.1 | `Forge/KMD Travel - Forge 1.20.1` |
| NeoForge | 1.21.1 | `NeoForge/KMD Travel - NeoForge 1.21.1` |
| NeoForge | 1.21.10 / 1.21.11 | `NeoForge/KMD Travel - NeoForge 1.21.10`, `NeoForge/KMD Travel - NeoForge 1.21.11` |
| Fabric | 1.21.1 | `fabric` |
| Fabric | 1.21.10 / 1.21.11 | `Fabric Versions/KMD Travel - Fabric 1.21.10`, `Fabric Versions/KMD Travel - Fabric 1.21.11` |

Each loader/version folder is intentionally separate so builds can be tested and released independently.

## Quick Start

1. Install the correct KMD Travel jar for your loader and Minecraft version.
2. Place a travel post in the world.
3. Right-click it to discover it.
4. Place and discover another travel post.
5. Right-click a discovered post to open the travel map and choose a destination.

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

Profiles can also be exported as JSON for easier editing and sharing. Template profiles can be placed in the modpack-level `kmdtravel/events` folder and are copied into new worlds.

## Configuration And Generated Files

KMD keeps modpack-level templates separate from world-specific data.

Typical generated folders:

```text
.minecraft/
└─ kmdtravel/
   ├─ events/              # template event profiles copied into new worlds
   ├─ recipes/             # exported editable recipe references
   └─ config/              # common mod config values

world save folder/
└─ kmdtravel/
   ├─ events/              # active world/server event profiles
   └─ map_cache/           # explored terrain cache
```

See the full documentation in [KMD WIKI](https://ninjasummoner.github.io/KMDTravelMod/#kmd-travel-wiki).

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

Forge 1.20.1 requires Java 17. Minecraft 1.21.x loaders require Java 21.

## Repository Layout

```text
.
├─ docs/                  # wiki-style documentation
├─ Forge/                 # Forge 1.20.1 project
├─ NeoForge/              # NeoForge projects by Minecraft version
├─ fabric/                # Fabric 1.21.1 project
└─ Fabric Versions/       # Fabric 1.21.10+ projects
```

## Addons And Modpacks

KMD Travel is source-available and addon-friendly. Addons, datapacks, recipe packs, translations, quest integrations, and compatibility mods are allowed when they depend on KMD Travel instead of bundling or republishing KMD Travel code/assets.

You may include official KMD Travel releases in public or private modpacks without asking first.

## License

KMD Travel uses the custom [`KMD Travel Source-Available License`](LICENSE.md). The source is visible for transparency, contribution, addon development, and compatibility work, but KMD Travel itself may not be rehosted, renamed, or redistributed as a standalone fork without permission.

## Credits

Created by **Ninjasummoner**.
