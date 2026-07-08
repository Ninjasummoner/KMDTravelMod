# Version Guide

KMD Travel keeps loader/version projects separate. This makes each release easier to test because Minecraft loader APIs change often.

## Primary maintained folders

| Loader | Minecraft | Java | Folder |
| --- | --- | --- | --- |
| Forge | 1.20.1 | 17 | `Forge/KMD Travel - Forge 1.20.1` |
| NeoForge | 1.21.1 | 21 | `NeoForge/KMD Travel - NeoForge 1.21.1` |
| NeoForge | 1.21.10 | 21 | `NeoForge/KMD Travel - NeoForge 1.21.10` |
| NeoForge | 1.21.11 | 21 | `NeoForge/KMD Travel - NeoForge 1.21.11` |
| Fabric | 1.21.1 | 21 | `fabric` |
| Fabric | 1.21.10 | 21 | `Fabric Versions/KMD Travel - Fabric 1.21.10` |
| Fabric | 1.21.11 | 21 | `Fabric Versions/KMD Travel - Fabric 1.21.11` |

## Building a release jar

Open the folder for the target loader/version and run:

```powershell
./gradlew.bat build
```

The jar appears in that folder's `build/libs` directory.

## Local files not committed

Do not commit these folders:

- `build/`
- `.gradle/`
- `run/`
- `config/`
- `saves/`
- generated map caches
- generated world event profile folders

## Release checklist

- Confirm the correct Minecraft version in `gradle.properties`.
- Build with the required Java version.
- Test placing and breaking all travel post variants.
- Test regular and shared post recipes.
- Test map opening, panning, zooming, labels, and discovered-area list.
- Test passive, hostile, skipped, and command-finished events.
- Test world-specific event profile saving.
