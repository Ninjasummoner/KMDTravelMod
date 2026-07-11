# Contributing to KMD Travel

Thanks for helping improve KMD Travel. Bug reports, compatibility fixes, documentation, translations, and focused pull requests are welcome.

## Before You Start

- Read [LICENSE.md](LICENSE.md). KMD Travel is source-available, not OSI open source.
- Keep addons separate from KMD itself and depend on the official mod.
- Open an issue before beginning a large redesign or new loader port.
- Do not commit generated worlds, map caches, run directories, Gradle caches, or built jars.

## Pick the Correct Project

| Loader | Minecraft | Java | Project folder |
| --- | --- | --- | --- |
| Forge | 1.20.1 | 17 | `Forge/KMD Travel - Forge 1.20.1` |
| NeoForge | 1.21.1 | 21 | repository root |
| NeoForge | 1.21.10 | 21 | `NeoForge/KMD Travel - NeoForge 1.21.10` |
| Fabric | 1.21.1 | 21 | `fabric` |
| Fabric | 1.21.10 | 21 | `Fabric Versions/KMD Travel - Fabric 1.21.10` |
| Fabric | 26.2 | 25 | `Fabric Versions/KMD Travel - Fabric 26.2` |

Loader projects intentionally remain separate because mappings, registries, networking, recipes, entity NBT, and rendering APIs differ by Minecraft version.

## Code Guidelines

- Make the smallest change that fixes the underlying problem.
- Match nearby naming and formatting.
- Keep loader-specific code inside its loader project.
- Preserve user-entered event NBT as a literal compound whenever the target Minecraft version accepts it.
- Avoid silent fallbacks that spawn entities or complete encounters the profile did not request.
- Do not add unrelated generated assets or build output.
- Document player-facing commands, configuration, or profile format changes.

## Testing

Run the Gradle wrapper from every project affected by your change:

```powershell
./gradlew.bat build
```

For gameplay changes, test the relevant behavior in a new world and an existing world. Event and map changes should cover:

- Regular and shared post discovery.
- World restart and server restart persistence.
- Map opening, panning, zooming, labels, and dimension separation.
- Passive, timed hostile, kill-based hostile, skipped, and failed-spawn encounters.
- Literal custom NBT, equipment, merchant offers, command delays, and cleanup.
- Two players travelling or encountering events at the same time.

## Pull Requests

Describe:

- The problem being fixed.
- The loader/version folders changed.
- How the change was tested.
- Any config, save-data, profile, recipe, or compatibility impact.

Keep generated files out of the pull request and avoid mixing unrelated refactors with behavior changes.
