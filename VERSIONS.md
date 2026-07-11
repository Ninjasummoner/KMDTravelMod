# Version Guide

KMD Travel keeps loader/version projects separate. Minecraft loader APIs change often, so each folder is built and tested independently.

## Maintained Folders

| Loader | Minecraft | Java | Folder |
| --- | --- | --- | --- |
| Forge | 1.20.1 | 17 | `Forge/KMD Travel - Forge 1.20.1` |
| NeoForge | 1.21.1 | 21 | root project |
| NeoForge | 1.21.10 | 21 | `NeoForge/KMD Travel - NeoForge 1.21.10` |
| Fabric | 1.21.1 | 21 | `fabric` |
| Fabric | 1.21.10 | 21 | `Fabric Versions/KMD Travel - Fabric 1.21.10` |
| Fabric | 26.2 | 25 | `Fabric Versions/KMD Travel - Fabric 26.2` |

## Building A Release Jar

Open the folder for the target loader/version and run:

```powershell
./gradlew.bat build
```

The jar appears in that folder's `build/libs` directory.

## Local Files Not Committed

Do not commit these folders:

- `build/`
- `.gradle/`
- `run/`
- `config/`
- `saves/`
- generated map caches
- generated world event profile folders

## Release Checklist

- Confirm the correct Minecraft version in `gradle.properties`.
- Build with the required Java version.
- Test placing and breaking all travel post variants.
- Test regular and shared post recipes, including disabled shared-post crafting.
- Test map opening, panning, zooming, labels, and discovered-area list.
- Test passive, hostile, skipped, and command-finished events.
- Test custom event NBT, merchant trades, and event command delays.
- Test world-specific event profile saving and template import.
