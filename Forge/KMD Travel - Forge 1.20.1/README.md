# KMD Travel

Kingdom Come inspired fast travel for Minecraft 1.20.1 Forge-compatible loaders.

## Current prototype

- Adds a two-block-tall Fast Travel Post.
- Renaming the item before placement names the post.
- Using a named Name Tag on an existing post renames it.
- Each player must right-click a post to discover it.
- Right-clicking a discovered post opens a parchment-style travel map.
- Travel costs hunger based on distance.
- Server-side travel works in singleplayer and multiplayer.
- Travel events can interrupt the trip, spawn mobs, and resume once the mobs are cleared.
- `/kmdtravel events true|false` toggles travel interruptions.

## Planned next layer

- Animated fade-to-black travel overlay.
- Moving player-face route marker during travel.
- Better parchment map art and biome route tinting.
- More event types with per-biome weighting.
- Optional cross-dimension support.
