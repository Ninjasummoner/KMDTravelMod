# Changelog

All notable changes to KMD Travel are tracked here.

## 0.3.0

### Added

- Added the animated handheld Travel Map with live cached terrain centered on the player.
- Added discovered and shared travel-post markers to the handheld map, including banner colors and patterns.
- Added a shaped Travel Map recipe using ink, feather, leather, paper, and ender pearls.

### Changed

- Reduced the handheld player-head marker for clearer terrain visibility.
- Kept handheld-map travel on the same resumable route flow used by travel posts after encounters.

### Fixed

- Fixed handheld player-head marker depth across immediate renderers and preserved deferred pose transforms so 1.21.10+ scroll animations and live map geometry render correctly.

## 0.2.0

### Added

- Added custom event profiles with in-game editing and JSON export/import.
- Added world/server-specific event profile storage and modpack-level profile templates.
- Added the bundled `KMD_Events` profile with custom mobs, merchant trades, biome rules, rare encounters, and safe refresh behavior for untouched bundled defaults.
- Added literal summon-style mob NBT, event commands, delays, dimensions, biomes, selection weights, avoid chance, passive timers, and kill-based hostile encounters.
- Added `/kmdtravel finishencounter` for admin, command-block, quest, and addon integrations.
- Added shared travel posts and server-controlled shared-post crafting.
- Added the parchment fast travel map, discovered-area panel, named pins, banner markers, zooming, panning, and dimension-aware terrain caching.
- Added a public Java addon API and addon-development documentation.
- Added maintained Forge 1.20.1, NeoForge 1.21.1/1.21.10, Fabric 1.21.1/1.21.10, and Fabric 26.2 projects.

### Changed

- Updated travel-post world and inventory models across wood variants.
- Improved map tile persistence so valid explored tiles survive restarts without requiring a complete chunk snapshot.
- Simplified parchment rendering and removed unused texture-slicing helpers.
- Kept custom event NBT intact instead of normalizing item counts, components, attributes, equipment, or trades.
- Refined event selection to use the route and selected encounter location before applying dimension, biome, and time rules.

### Fixed

- Fixed encounters started from the handheld Travel Map reopening an empty destination list when travel resumed.
- Rebuilt resumed travel screens from the preserved route source so removed, unloaded, or synthetic source posts do not interrupt the remaining journey.

- Fixed newly spawned encounter mobs occasionally being treated as already dead before Minecraft registered them in the level.
- Fixed completed encounters leaving tracked mobs, boats, or temporary NPCs behind.
- Fixed custom NBT being lost when editing and saving an event.
- Fixed map cache tiles disappearing after restart and placeholder colors being persisted as explored terrain.
- Fixed event-profile defaults leaking between worlds and improved migration of untouched legacy defaults.
- Fixed several cross-loader recipe, item-model, map-label, and UI parity issues.

### Maintenance

- Removed verified unused imports and obsolete map-cache/rendering helpers.
- Updated repository documentation, version guidance, event references, issue templates, and contribution instructions.
