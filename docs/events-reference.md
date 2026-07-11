# KMD Default Event Profile

This page documents the shipped `KMD_Events` profile. It is generated into every loader/version from `generate_default_profiles.py`.

## NBT Format Notes

- Forge 1.20.1 uses older item stack NBT inside entity equipment: `Count` and `tag`.
- Minecraft 1.21+ uses component-style item stack data in equipment: `count` and `components`.
- The generator emits the right format per loader, so the same event idea works on each supported version.
- Hostile custom mobs receive weapons or natural hostile AI, persistence, custom names, and themed gear.
- Villagers and wandering traders include `Offers`, so passive merchant stops have actual trades.

## Event List

| Event | Type | Dimension | Biome | Time | What happens |
|---|---|---|---|---|---|
| Meadow Hawker | Passive | `minecraft:overworld` | `minecraft:plains` | DAY | A cheerful hawker waves from the grass. |
| Toll Road Ruffians | Hostile | `minecraft:overworld` | `minecraft:plains` | BOTH | Armed ruffians block the king's road. |
| Moonfang Wolves | Hostile | `minecraft:overworld` | `minecraft:meadow` | NIGHT | Yellow eyes gather beyond the wildflowers. |
| Charcoal Burner | Passive | `minecraft:overworld` | `minecraft:forest` | DAY | A charcoal burner shares warm directions and useful fuel. |
| Forest Poachers | Hostile | `minecraft:overworld` | `minecraft:forest` | DAY | Poachers spring from between the trees. |
| Dark Forest Whisper | Hostile | `minecraft:overworld` | `minecraft:dark_forest` | NIGHT | A whisper circles the black oaks. |
| Fey Ring Bargain | Passive | `minecraft:overworld` | `minecraft:flower_forest` | NIGHT | Fey lanterns offer a harmless bargain under the trees. |
| Taiga Trapper | Passive | `minecraft:overworld` | `minecraft:taiga` | DAY | A trapper trades gossip beside pine smoke. |
| Snowblind Strays | Hostile | `minecraft:overworld` | `minecraft:snowy_plains` | BOTH | Strays rise from the white wind. |
| Glass-Ice Knights | Hostile | `minecraft:overworld` | `minecraft:ice_spikes` | DAY | Crystal-bright knights stalk the blue ice. |
| Jungle Ruin Curse | Hostile | `minecraft:overworld` | `minecraft:jungle` | NIGHT | The vines shake around a forgotten idol. |
| Swamp Lanterns | Passive | `minecraft:overworld` | `minecraft:swamp` | NIGHT | Lanterns bob across the reeds. |
| Mangrove Bog Ambush | Hostile | `minecraft:overworld` | `minecraft:mangrove_swamp` | BOTH | Mud bubbles, and shapes lurch forward. |
| Desert Water Seller | Passive | `minecraft:overworld` | `minecraft:desert` | DAY | A water seller shades a clay jug. |
| Sun Raiders | Hostile | `minecraft:overworld` | `minecraft:desert` | DAY | Raiders shimmer out of the heat haze. |
| Badlands Bone Toll | Hostile | `minecraft:overworld` | `minecraft:badlands` | NIGHT | Bone-white bandits demand payment. |
| Savanna Caravan | Passive | `minecraft:overworld` | `minecraft:savanna` | DAY | A caravan pauses long enough to trade trail goods. |
| Spear Band | Hostile | `minecraft:overworld` | `minecraft:savanna` | BOTH | A lean warband tracks you through the dry grass. |
| Goat Hermit | Passive | `minecraft:overworld` | `minecraft:stony_peaks` | DAY | A highland hermit offers mountain wisdom. |
| Mountain Outlaws | Hostile | `minecraft:overworld` | `minecraft:jagged_peaks` | DAY | Outlaws drop from the ridge line. |
| Glow Spore Drift | Passive | `minecraft:overworld` | `minecraft:lush_caves` | BOTH | Glowing spores drift through the passage. |
| Dripstone Cave Robbers | Hostile | `minecraft:overworld` | `minecraft:dripstone_caves` | BOTH | Robbers rattle lanterns beneath the stone teeth. |
| Deep Dark Heartbeat | Hostile | `minecraft:overworld` | `minecraft:deep_dark` | BOTH | Something below knows your name. |
| Reef Pirates | Hostile | `minecraft:overworld` | `minecraft:beach` | BOTH | Pirates row from the reef line. |
| Floating Merchant | Passive | `minecraft:overworld` | `minecraft:river` | DAY | A floating merchant bumps against the reeds. |
| Moon-Drowned Bells | Hostile | `minecraft:overworld` | `minecraft:ocean` | NIGHT | Bells ring beneath the water. |
| Pearl Diver | Passive | `minecraft:overworld` | `minecraft:warm_ocean` | DAY | A pearl diver shares a tale of the current. |
| Icewreck Crew | Hostile | `minecraft:overworld` | `minecraft:frozen_ocean` | BOTH | An old icewreck crew claws from the floes. |
| Mushroom Forager | Passive | `minecraft:overworld` | `minecraft:mushroom_fields` | DAY | A red-robed forager sells strange stew stories. |
| Cherry Lantern Thieves | Hostile | `minecraft:overworld` | `minecraft:cherry_grove` | NIGHT | Lantern thieves scatter petals in your path. |
| Nether Barbecue | Passive | `minecraft:the_nether` | `minecraft:nether_wastes` | BOTH | A nervous roaster tends a tiny furnace-camp. |
| Piglin Gold Patrol | Hostile | `minecraft:the_nether` | `minecraft:crimson_forest` | BOTH | A gold patrol mistakes you for a thief. |
| Soul Valley Revenants | Hostile | `minecraft:the_nether` | `minecraft:soul_sand_valley` | NIGHT | Blue fire reveals old bones. |
| Obsidian Mirror Bandits | Hostile | `minecraft:the_nether` | `minecraft:basalt_deltas` | BOTH | Mirror-dark bandits step out of the heat shimmer. |
| Warped Oracle | Passive | `minecraft:the_nether` | `minecraft:warped_forest` | BOTH | A warped oracle listens to the fungus breathe. |
| End Pearl Scholar | Passive | `minecraft:the_end` | `minecraft:end_highlands` | BOTH | A silent scholar studies the islands. |
| End Barrens Hunters | Hostile | `minecraft:the_end` | `minecraft:end_barrens` | NIGHT | Hunters flicker between the stones. |
| Voidglass Duelists | Hostile | `minecraft:the_end` | `minecraft:small_end_islands` | BOTH | Voidglass duelists bow once, then vanish between strikes. |
| Royal Relic Peddler | Passive | `minecraft:overworld` | `Global` | BOTH | A velvet-cloaked peddler opens a box of minor relics. |
| Clockwork Toll Collectors | Hostile | `minecraft:overworld` | `Global` | BOTH | Brass-masked collectors step from the dust and demand payment. |
| Witchlight Apothecary | Passive | `minecraft:overworld` | `Global` | BOTH | A green lantern reveals an apothecary with road remedies. |
| Ember Relic Guard | Hostile | `minecraft:the_nether` | `Global` | BOTH | An ember-armored guard mistakes you for a tomb robber. |

## Version-Specific Events

The Forge 1.20.1, NeoForge 1.21.1, and Fabric 1.21.1 profiles contain 42 events. Minecraft 1.21.10 and newer profiles contain 44 events because they can use content that does not exist in older versions:

| Event | Type | Dimension | Biome | Time | What happens |
|---|---|---|---|---|---|
| Pale Garden Creak | Hostile | `minecraft:overworld` | `minecraft:pale_garden` | BOTH | A creaking-themed encounter stalks the pale trees. |
| Pale Garden Moth | Passive | `minecraft:overworld` | `minecraft:pale_garden` | BOTH | A quiet fantasy encounter appears among the pale growth. |

## Example Highlights

### Charcoal Burner
A forest passive event that spawns a wandering trader named Charcoal Burner. The trader sells coal, charcoal, and campfires.

### Toll Road Ruffians
A plains hostile event that spawns armed pillagers with custom names and themed leather gear. It ends when the spawned mobs are killed.

### Rare Clockwork Toll Collectors
A very low-weight global hostile event with glowing brass-colored vindicators and a small copper reward after completion.

## Design Rules Used

- No default event applies harmful potion effects to the player.
- Commands are limited to harmless sounds and small completion rewards.
- Passive merchant events use trades instead of plain villagers.
- Hostile events spawn close to the player by default so encounters start quickly.
- Events are biome-targeted again, with a few rare global events for surprise variety.
