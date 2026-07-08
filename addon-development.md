---
title: Addon Development
layout: default
nav_order: 2
---

# Addon Development

This page explains the safest ways to build around KMD Travel without copying or republishing KMD itself.

KMD is designed to be extended through **dependencies, event profiles, datapacks, commands, config files, and compatibility mods**. The safest addon is a separate project that requires KMD Travel and adds content around it.

## Quick Rules

Allowed addon patterns:

- Require KMD Travel as a dependency.
- Add custom event profiles.
- Add datapack recipe overrides.
- Add quest-book or command-block integrations.
- Add compatibility with modded mobs, dimensions, biomes, or structures.
- Call KMD commands from your own mod, quest, script, or datapack flow.

Avoid:

- Bundling KMD Travel inside your addon jar.
- Copying KMD source code into your addon.
- Copying KMD models, textures, sounds, UI art, or branding into your addon.
- Publishing a modified KMD jar as your addon.

See the project `LICENSE.md` for the full license terms.

## Stable Extension Points

These are the parts addon makers should prefer because they are least likely to break:

| Extension Point | Best For | Stability |
| --- | --- | --- |
| Event profile JSON | New encounters, custom mobs, biome/dimension rules | Recommended |
| Minecraft commands | Quest mods, command blocks, scripted events | Recommended |
| Datapack recipes | Changing crafting recipes | Recommended |
| Resource packs | Personal/server visual changes | Safe if assets are original |
| Mod dependencies | Java/Kotlin compatibility mods | Good, but avoid internals |
| Internal KMD classes | Direct code calls into KMD systems | Not stable unless documented later |

## Mod ID And IDs

KMD Travel uses this mod id:

```text
kmdtravel
```

Common IDs:

```text
kmdtravel:fast_travel_post
kmdtravel:spruce_fast_travel_post
kmdtravel:birch_fast_travel_post
kmdtravel:jungle_fast_travel_post
kmdtravel:acacia_fast_travel_post
kmdtravel:dark_oak_fast_travel_post
kmdtravel:mangrove_fast_travel_post
kmdtravel:cherry_fast_travel_post
kmdtravel:shared_fast_travel_post
```

Use these IDs for recipes, commands, tags, datapacks, and compatibility checks.

## Adding KMD As A Dependency

### Fabric `fabric.mod.json`

Use `depends` if your addon requires KMD to load:

```json
{
  "schemaVersion": 1,
  "id": "my_kmd_addon",
  "version": "1.0.0",
  "name": "My KMD Addon",
  "depends": {
    "fabricloader": ">=0.16.0",
    "minecraft": "~1.21.1",
    "kmdtravel": ">=0.2.0"
  }
}
```

Use `suggests` only if your addon can still work without KMD:

```json
{
  "suggests": {
    "kmdtravel": ">=0.2.0"
  }
}
```

### NeoForge `neoforge.mods.toml`

```toml
[[dependencies.my_kmd_addon]]
modId="kmdtravel"
type="required"
versionRange="[0.2.0,)"
ordering="AFTER"
side="BOTH"
```

### Forge 1.20.1 `mods.toml`

```toml
[[dependencies.my_kmd_addon]]
modId="kmdtravel"
mandatory=true
versionRange="[0.2.0,)"
ordering="AFTER"
side="BOTH"
```

## Gradle Development Setup

If KMD is not published to a Maven yet, the simplest local addon setup is to place a KMD jar in your addon's `libs` folder.

### Fabric Example

```gradle
dependencies {
    modImplementation files("libs/kmdtravel-fabric-0.2.0.jar")
}
```

### NeoForge Example

```gradle
dependencies {
    implementation files("libs/kmdtravel-neoforge-0.2.0.jar")
}
```

### Forge Example

```gradle
dependencies {
    implementation fg.deobf(files("libs/kmdtravel-forge-0.2.0.jar"))
}
```

If KMD is later published to a Maven, prefer the Maven dependency instead of a local `libs` jar.

## Event Profile Addons

The most addon-friendly way to add content is to ship or document KMD event profile JSON.

KMD event profiles are world/server specific. Server owners can place templates in:

```text
.minecraft/kmdtravel/events/
```

When a new world is created, KMD copies template profiles into that world's event folder.

A world-specific event folder looks like:

```text
.minecraft/kmdtravel/events/<world-name>_<world-id>/
```

Example:

```text
.minecraft/kmdtravel/events/my_world_a1b2c3d4/goblin_roads.json
```

### Minimal Event Profile Example

```json
{
  "id": "goblin_roads",
  "name": "Goblin Roads",
  "events": [
    {
      "id": "goblin_ambush",
      "enabled": true,
      "title": "Goblin Ambush",
      "description": "A pack of goblins rushes from the roadside.",
      "passive": false,
      "endsByKill": true,
      "durationSeconds": 60,
      "dimension": "minecraft:overworld",
      "biome": "",
      "timeOfDay": "BOTH",
      "selectionWeight": 1.0,
      "avoidChance": 0.15,
      "mobs": [
        {
          "mobId": "minecraft:zombie",
          "amount": 3,
          "spawnRange": 16,
          "displayName": "Goblin",
          "nbt": "{PersistenceRequired:1b}"
        }
      ],
      "commands": []
    }
  ]
}
```

Field names may change as KMD develops. If an event does not load, create a similar event in the in-game editor, save it, then compare the generated JSON.

## Custom Mob NBT

KMD stores mob NBT as plain text SNBT inside event JSON.

Simple example:

```snbt
{PersistenceRequired:1b}
```

Named skeleton with a sword:

```snbt
{CustomName:'{"text":"Road Warden","color":"gold"}',PersistenceRequired:1b,HandItems:[{id:"minecraft:iron_sword",count:1b},{id:"minecraft:air",count:0b}]}
```

Slow invisible mob with only a helmet/head visible:

```snbt
{Invisible:1b,Silent:1b,CustomNameVisible:0b,PersistenceRequired:1b,Attributes:[{Name:"minecraft:generic.movement_speed",Base:0.08}],ArmorItems:[{id:"minecraft:air",count:0b},{id:"minecraft:air",count:0b},{id:"minecraft:air",count:0b},{id:"minecraft:player_head",count:1b}],HandItems:[{id:"minecraft:air",count:0b},{id:"minecraft:air",count:0b}]}
```

Minecraft 1.20.1 and 1.21.x use different item/NBT formats in some places. If a complex head or component fails, test it first with Minecraft's `/summon` command for that exact version.

## Event Commands

KMD event commands run through KMD, so they can use KMD placeholders.

Supported placeholders:

```text
{player}
{uuid}
{x}
{y}
{z}
{event_tag}
```

Examples:

```mcfunction
effect give {player} minecraft:blindness 10 1
playsound minecraft:entity.ghast.scream host {player} ~ ~ ~ 1 0.4
title {player} actionbar {"text":"Something watches you...","color":"dark_red"}
summon minecraft:zombie {x} {y} {z} {Tags:["{event_tag}"]}
```

Important distinction:

- `{player}` works only inside KMD event commands.
- Command blocks, chat, and quest mods should use real selectors like `@p`, `@a`, or a player name.

## Finishing Encounters From Addons Or Quest Mods

Use this command when an external system decides the encounter is complete:

```mcfunction
/kmdtravel finishencounter <target>
```

Examples:

```mcfunction
/kmdtravel finishencounter @p
/kmdtravel finishencounter Ninjasummoner
```

Good use cases:

- FTB Quests reward command.
- Command block puzzle completion.
- Server script after a boss dies.
- Custom addon event logic.

Do not type `<target>` literally. Use a selector or player name.

## Calling KMD Commands From A Fabric Addon

This avoids relying on KMD internals. Your addon only needs KMD installed and then can execute KMD commands through the server command system.

```java
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;

public final class MyKmdAddon {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("myaddon_finish_kmd")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    String command = "kmdtravel finishencounter @p";
                    context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), command);
                    return 1;
                }));
        });
    }
}
```

## Calling KMD Commands From A NeoForge Addon

```java
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MyKmdAddonCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("myaddon_finish_kmd")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                String command = "kmdtravel finishencounter @p";
                context.getSource().getServer().getCommands().performPrefixedCommand(context.getSource(), command);
                return 1;
            }));
    }
}
```

## Recipe Addons

Use datapacks to replace KMD recipes.

Example path for modern versions:

```text
data/kmdtravel/recipe/shared_fast_travel_post.json
```

Example shaped recipe:

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "DCD",
    "FFF",
    "DND"
  ],
  "key": {
    "D": { "item": "minecraft:diamond" },
    "C": { "item": "minecraft:compass" },
    "F": { "item": "minecraft:nether_brick_fence" },
    "N": { "item": "minecraft:nether_brick" }
  },
  "result": {
    "id": "kmdtravel:shared_fast_travel_post",
    "count": 1
  }
}
```

Minecraft 1.20.1 may use slightly different recipe result syntax and the older `recipes` folder naming in some loader setups.

## Tags And Compatibility

If your addon needs to identify KMD blocks/items, prefer IDs and tags rather than copying classes.

Example item tag:

```text
data/my_kmd_addon/tags/item/kmd_posts.json
```

```json
{
  "replace": false,
  "values": [
    "kmdtravel:fast_travel_post",
    "kmdtravel:spruce_fast_travel_post",
    "kmdtravel:birch_fast_travel_post",
    "kmdtravel:jungle_fast_travel_post",
    "kmdtravel:acacia_fast_travel_post",
    "kmdtravel:dark_oak_fast_travel_post",
    "kmdtravel:mangrove_fast_travel_post",
    "kmdtravel:cherry_fast_travel_post",
    "kmdtravel:shared_fast_travel_post"
  ]
}
```

## Suggested Addon Types

### Event Pack Addon

Adds themed JSON profiles for a modpack or server.

Examples:

- Undead roads.
- Pirate coasts.
- Nether raids.
- Fantasy kingdom encounters.

### Quest Integration Addon

Uses quest rewards or server commands to:

- Assign KMD profiles.
- Finish encounters.
- Trigger effects.
- Unlock new travel danger phases.

### Datapack Recipe Addon

Changes crafting costs for:

- Regular travel posts.
- Shared travel posts.
- Server economy balance.

### Compatibility Addon

Adds event profiles using mobs from another mod.

Example:

```json
{
  "mobId": "examplemod:troll",
  "amount": 1,
  "spawnRange": 24,
  "displayName": "Road Troll",
  "nbt": "{PersistenceRequired:1b}"
}
```

## What Is Not Stable Yet

The following are internal implementation details and may change between KMD versions:

- Client map rendering classes.
- Network packet class names.
- In-game editor screen internals.
- Fast travel route/session internals.
- Saved-data implementation classes.

If you need one of these to be stable, open an issue asking for a small public API. That is better than depending on internals that may change.

