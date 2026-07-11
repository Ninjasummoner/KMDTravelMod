
from pathlib import Path
import base64
import re

ROOT = Path(__file__).resolve().parent
TARGETS = [
    ROOT,
    ROOT / "fabric",
    ROOT / "Forge" / "KMD Travel - Forge 1.20.1",
    ROOT / "NeoForge" / "KMD Travel - NeoForge 1.21.10",
    ROOT / "Fabric Versions" / "KMD Travel - Fabric 1.21.10",
    ROOT / "Fabric Versions" / "KMD Travel - Fabric 26.2",
]

PROFILE_ID = "kmd_events"
PROFILE_NAME = "KMD_Events"
SPAWN_RANGE = 8

DYES = {
    "bandit": 0x6B2E1E, "forest": 0x2F5D2F, "frost": 0x9FD7FF,
    "desert": 0xD2A24C, "nether": 0x5B1A16, "end": 0x4B2B6F,
    "sea": 0x1C6A7A, "shadow": 0x26222D, "gold": 0xD6A33B,
    "swamp": 0x355E3B, "pale": 0xC8C8B8,
}

HOSTILE = {
    "minecraft:pillager": ("minecraft:crossbow", "bandit", "red"),
    "minecraft:vindicator": ("minecraft:iron_axe", "bandit", "red"),
    "minecraft:skeleton": ("minecraft:bow", "shadow", "dark_gray"),
    "minecraft:stray": ("minecraft:bow", "frost", "aqua"),
    "minecraft:zombie": ("minecraft:iron_sword", "forest", "dark_green"),
    "minecraft:husk": ("minecraft:stone_sword", "desert", "gold"),
    "minecraft:drowned": ("minecraft:trident", "sea", "aqua"),
    "minecraft:piglin": ("minecraft:golden_sword", "nether", "gold"),
    "minecraft:wither_skeleton": ("minecraft:stone_sword", "nether", "dark_red"),
    "minecraft:enderman": ("minecraft:air", "end", "light_purple"),
    "minecraft:wolf": ("minecraft:air", "shadow", "gray"),
    "minecraft:slime": ("minecraft:air", "swamp", "green"),
}


def b64(text):
    return base64.b64encode(text.encode("utf-8")).decode("ascii")


def java_str(text):
    return '"' + text.replace('\\', '\\\\').replace('"', '\\"') + '"'


def json_name(name, color="gold"):
    safe = name.replace("\\", "").replace("'", "").replace('"', "")
    return "CustomName:'{\"text\":\"%s\",\"color\":\"%s\",\"italic\":false}'" % (safe, color)


def item(item_id, count, modern):
    key = "count" if modern else "Count"
    return '{id:"%s",%s:%db}' % (item_id, key, count)


def leather(item_id, color, modern):
    if modern:
        return '{id:"%s",count:1b,components:{"minecraft:dyed_color":{rgb:%d}}}' % (item_id, color)
    return '{id:"%s",Count:1b,tag:{display:{color:%d}}}' % (item_id, color)


def offer(row, modern):
    buy_item, buy_count, sell_item, sell_count, uses = row
    return '{buy:%s,sell:%s,maxUses:%d,rewardExp:false,priceMultiplier:0.05f}' % (
        item(buy_item, buy_count, modern), item(sell_item, sell_count, modern), uses
    )


def offers(rows, modern):
    return "Offers:{Recipes:[%s]}" % ",".join(offer(row, modern) for row in rows)


def trader_nbt(name, modern, trades, color="gold"):
    return "{%s,PersistenceRequired:1b,%s}" % (json_name(name, color), offers(trades, modern))


def villager_nbt(name, modern, profession, trades, color="gold"):
    return "{%s,PersistenceRequired:1b,VillagerData:{profession:\"minecraft:%s\",level:2,type:\"minecraft:plains\"},%s}" % (
        json_name(name, color), profession, offers(trades, modern)
    )


def hostile_nbt(entity, name, modern, palette=None, weapon=None, glowing=False, health=24):
    default_weapon, default_palette, name_color = HOSTILE.get(entity, ("minecraft:iron_sword", "bandit", "red"))
    palette = palette or default_palette
    weapon = weapon or default_weapon
    parts = [json_name(name, name_color), "PersistenceRequired:1b", "Health:%df" % health, 'Attributes:[{Name:"generic.follow_range",Base:40.0d}]']
    if glowing:
        parts.append("Glowing:1b")
    if entity not in ("minecraft:enderman", "minecraft:wolf", "minecraft:slime"):
        color = DYES.get(palette, DYES["bandit"])
        armor = [leather("minecraft:leather_boots", color, modern), leather("minecraft:leather_leggings", color, modern), leather("minecraft:leather_chestplate", color, modern), leather("minecraft:leather_helmet", color, modern)]
        parts += ["ArmorItems:[%s]" % ",".join(armor), "ArmorDropChances:[0.02f,0.02f,0.02f,0.02f]", "HandItems:[%s,%s]" % (item(weapon, 0 if weapon == "minecraft:air" else 1, modern), item("minecraft:air", 0, modern)), "HandDropChances:[0.05f,0.0f]"]
    return "{%s}" % ",".join(parts)


def creature_nbt(name, color="aqua", glowing=True):
    parts = [json_name(name, color), "PersistenceRequired:1b"]
    if glowing:
        parts.append("Glowing:1b")
    return "{%s}" % ",".join(parts)


def mob_entry(entity, amount, spawn_range, name, nbt):
    return f"{entity}|{amount}|{spawn_range}|{b64(name)}|{b64(nbt) if nbt else ''}"


def cmd(delay, text):
    return delay, text


def ev(event_id, title, passive, duration, desc, dim, biome, time, completion, weight, avoid, mobs, commands=None):
    return dict(id=event_id, title=title, passive=passive, duration=duration, desc=desc, dim=dim, biome=biome, time=time, completion=completion, weight=weight, avoid=avoid, mobs=mobs, commands=commands or [])


def make_events(modern, include_pale):
    T = lambda name, trades, color="gold": trader_nbt(name, modern, trades, color)
    V = lambda name, profession, trades, color="gold": villager_nbt(name, modern, profession, trades, color)
    H = lambda entity, name, palette=None, weapon=None, glowing=False, health=24: hostile_nbt(entity, name, modern, palette, weapon, glowing, health)
    C = creature_nbt
    e = []

    def add(event_id, title, passive, duration, desc, dim, biome, time, completion, weight, avoid, mob_specs, commands=None):
        mobs = []
        for spec in mob_specs:
            entity, amount, name, nbt = spec[:4]
            spawn_range = spec[4] if len(spec) > 4 else SPAWN_RANGE
            mobs.append(mob_entry(entity, amount, spawn_range, name, nbt))
        e.append(ev(event_id, title, passive, duration, desc, dim, biome, time, completion, weight, avoid, mobs, commands))

    add("plains_meadow_hawker", "Meadow Hawker", True, 45, "A cheerful hawker waves from the grass.", "minecraft:overworld", "minecraft:plains", "DAY", "TIMED", 1.2, 0.45,
        [("minecraft:wandering_trader", 1, "Meadow Hawker", T("Meadow Hawker", [("minecraft:emerald",1,"minecraft:bread",3,10),("minecraft:emerald",1,"minecraft:torch",8,8),("minecraft:emerald",2,"minecraft:lead",1,4)]))],
        [cmd(0,"playsound minecraft:entity.wandering_trader.ambient ambient {player} ~ ~ ~ 0.8 1.1")])
    add("plains_toll_ruffians", "Toll Road Ruffians", False, 60, "Armed ruffians block the king's road.", "minecraft:overworld", "minecraft:plains", "BOTH", "KILL", 1.1, 0.15,
        [("minecraft:pillager",2,"Road Corsair",H("minecraft:pillager","Road Corsair"))], [cmd(0,"playsound minecraft:item.crossbow.loading_middle hostile {player} ~ ~ ~ 0.8 0.9")])
    add("meadow_moonfang_wolves", "Moonfang Wolves", False, 50, "Yellow eyes gather beyond the wildflowers.", "minecraft:overworld", "minecraft:meadow", "NIGHT", "KILL", 0.9, 0.1,
        [("minecraft:wolf",3,"Moonfang Wolf",H("minecraft:wolf","Moonfang Wolf","shadow"))], [cmd(0,"playsound minecraft:entity.wolf.growl hostile {player} ~ ~ ~ 0.9 0.75")])

    add("forest_charcoal_burner", "Charcoal Burner", True, 50, "A charcoal burner shares warm directions and useful fuel.", "minecraft:overworld", "minecraft:forest", "DAY", "TIMED", 1.1, 0.5,
        [("minecraft:wandering_trader",1,"Charcoal Burner",T("Charcoal Burner", [("minecraft:emerald",1,"minecraft:coal",8,10),("minecraft:emerald",1,"minecraft:charcoal",8,10),("minecraft:emerald",2,"minecraft:campfire",1,4)], "dark_green"))], [cmd(0,"playsound minecraft:block.campfire.crackle ambient {player} ~ ~ ~ 0.9 1.0")])
    add("forest_poachers", "Forest Poachers", False, 60, "Poachers spring from between the trees.", "minecraft:overworld", "minecraft:forest", "DAY", "KILL", 1.2, 0.12,
        [("minecraft:skeleton",2,"Greenwood Poacher",H("minecraft:skeleton","Greenwood Poacher","forest","minecraft:bow"))], [cmd(0,"playsound minecraft:entity.skeleton.ambient hostile {player} ~ ~ ~ 0.8 1.0")])
    add("dark_forest_whisper", "Dark Forest Whisper", False, 55, "A whisper circles the black oaks.", "minecraft:overworld", "minecraft:dark_forest", "NIGHT", "KILL", 1.0, 0.08,
        [("minecraft:enderman",1,"Whisper in the Oaks",H("minecraft:enderman","Whisper in the Oaks","end",glowing=True,health=36))], [cmd(0,"playsound minecraft:entity.enderman.stare hostile {player} ~ ~ ~ 0.75 0.7")])
    add("flower_forest_fey_ring", "Fey Ring Bargain", True, 55, "Fey lanterns offer a harmless bargain under the trees.", "minecraft:overworld", "minecraft:flower_forest", "NIGHT", "TIMED", 0.9, 0.55,
        [("minecraft:allay",2,"Fey Lantern",C("Fey Lantern","light_purple"))], [cmd(0,"playsound minecraft:entity.allay.ambient_with_item ambient {player} ~ ~ ~ 0.8 1.2"), cmd(999,"give {player} minecraft:glow_ink_sac 1")])

    add("taiga_trapper", "Taiga Trapper", True, 45, "A trapper trades gossip beside pine smoke.", "minecraft:overworld", "minecraft:taiga", "DAY", "TIMED", 1.0, 0.45,
        [("minecraft:wandering_trader",1,"Taiga Trapper",T("Taiga Trapper", [("minecraft:emerald",1,"minecraft:sweet_berries",8,8),("minecraft:emerald",2,"minecraft:leather",2,6),("minecraft:emerald",1,"minecraft:string",6,8)], "dark_green"))], [cmd(0,"playsound minecraft:entity.villager.ambient ambient {player} ~ ~ ~ 0.7 0.8")])
    add("snowblind_strays", "Snowblind Strays", False, 70, "Strays rise from the white wind.", "minecraft:overworld", "minecraft:snowy_plains", "BOTH", "KILL", 1.3, 0.1,
        [("minecraft:stray",3,"Snowblind Stray",H("minecraft:stray","Snowblind Stray","frost","minecraft:bow",health=22))], [cmd(0,"playsound minecraft:entity.stray.ambient hostile {player} ~ ~ ~ 0.9 0.85")])
    add("ice_spike_glass_knights", "Glass-Ice Knights", False, 65, "Crystal-bright knights stalk the blue ice.", "minecraft:overworld", "minecraft:ice_spikes", "DAY", "KILL", 0.9, 0.08,
        [("minecraft:stray",2,"Glass-Ice Knight",H("minecraft:stray","Glass-Ice Knight","frost","minecraft:bow",glowing=True,health=28))], [cmd(0,"playsound minecraft:block.glass.break hostile {player} ~ ~ ~ 0.6 0.7"), cmd(999,"give {player} minecraft:amethyst_shard 1")])

    add("jungle_ruin_curse", "Jungle Ruin Curse", False, 55, "The vines shake around a forgotten idol.", "minecraft:overworld", "minecraft:jungle", "NIGHT", "KILL", 1.2, 0.1,
        [("minecraft:zombie",2,"Jungle Bonewalker",H("minecraft:zombie","Jungle Bonewalker","forest","minecraft:iron_sword"))], [cmd(0,"playsound minecraft:ambient.cave hostile {player} ~ ~ ~ 0.7 0.7")])
    add("swamp_lanterns", "Swamp Lanterns", True, 50, "Lanterns bob across the reeds.", "minecraft:overworld", "minecraft:swamp", "NIGHT", "TIMED", 0.9, 0.5,
        [("minecraft:villager",1,"Reed Herbalist",V("Reed Herbalist", "cleric", [("minecraft:emerald",1,"minecraft:lily_pad",4,8),("minecraft:emerald",1,"minecraft:brown_mushroom",6,8),("minecraft:emerald",2,"minecraft:glow_berries",3,5)], "dark_green"))], [cmd(0,"playsound minecraft:block.brewing_stand.brew ambient {player} ~ ~ ~ 0.8 1.2")])
    add("mangrove_bog_ambush", "Mangrove Bog Ambush", False, 60, "Mud bubbles, and shapes lurch forward.", "minecraft:overworld", "minecraft:mangrove_swamp", "BOTH", "KILL", 1.1, 0.12,
        [("minecraft:slime",2,"Bog Gelatin",H("minecraft:slime","Bog Gelatin","swamp")), ("minecraft:zombie",1,"Bog Walker",H("minecraft:zombie","Bog Walker","swamp","minecraft:stone_sword"))], [cmd(0,"playsound minecraft:entity.fishing_bobber.splash hostile {player} ~ ~ ~ 0.8 0.8")])

    add("desert_water_seller", "Desert Water Seller", True, 45, "A water seller shades a clay jug.", "minecraft:overworld", "minecraft:desert", "DAY", "TIMED", 1.0, 0.45,
        [("minecraft:wandering_trader",1,"Water Seller",T("Water Seller", [("minecraft:emerald",1,"minecraft:glass_bottle",3,8),("minecraft:emerald",2,"minecraft:cactus",6,6),("minecraft:emerald",2,"minecraft:bread",5,6)], "yellow"))], [cmd(0,"playsound minecraft:entity.wandering_trader.ambient ambient {player} ~ ~ ~ 0.8 0.9")])
    add("desert_sun_raiders", "Sun Raiders", False, 60, "Raiders shimmer out of the heat haze.", "minecraft:overworld", "minecraft:desert", "DAY", "KILL", 1.2, 0.08,
        [("minecraft:husk",3,"Sun-Scorched Raider",H("minecraft:husk","Sun-Scorched Raider","desert","minecraft:stone_sword"))], [cmd(0,"playsound minecraft:entity.husk.ambient hostile {player} ~ ~ ~ 0.9 0.9")])
    add("badlands_bone_toll", "Badlands Bone Toll", False, 60, "Bone-white bandits demand payment.", "minecraft:overworld", "minecraft:badlands", "NIGHT", "KILL", 1.1, 0.09,
        [("minecraft:skeleton",3,"Bone Toller",H("minecraft:skeleton","Bone Toller","desert","minecraft:bow"))], [cmd(0,"playsound minecraft:entity.skeleton.ambient hostile {player} ~ ~ ~ 0.8 0.7")])
    add("savanna_caravan", "Savanna Caravan", True, 50, "A caravan pauses long enough to trade trail goods.", "minecraft:overworld", "minecraft:savanna", "DAY", "TIMED", 1.0, 0.45,
        [("minecraft:wandering_trader",1,"Caravan Quartermaster",T("Caravan Quartermaster", [("minecraft:emerald",1,"minecraft:wheat",6,8),("minecraft:emerald",2,"minecraft:lead",1,4),("minecraft:emerald",1,"minecraft:hay_block",1,5)], "gold"))], [cmd(0,"playsound minecraft:entity.wandering_trader.yes ambient {player} ~ ~ ~ 0.8 1.0")])
    add("savanna_spear_band", "Spear Band", False, 55, "A lean warband tracks you through the dry grass.", "minecraft:overworld", "minecraft:savanna", "BOTH", "KILL", 1.1, 0.1,
        [("minecraft:pillager",2,"Spear Band Scout",H("minecraft:pillager","Spear Band Scout","desert","minecraft:crossbow"))], [cmd(0,"playsound minecraft:event.raid.horn hostile {player} ~ ~ ~ 0.7 1.1")])
    add("mountain_goat_hermit", "Goat Hermit", True, 45, "A highland hermit offers mountain wisdom.", "minecraft:overworld", "minecraft:stony_peaks", "DAY", "TIMED", 0.9, 0.5,
        [("minecraft:wandering_trader",1,"Goat Hermit",T("Goat Hermit", [("minecraft:emerald",1,"minecraft:snowball",8,8),("minecraft:emerald",2,"minecraft:leather",2,6),("minecraft:emerald",1,"minecraft:bread",3,8)], "white"))], [cmd(0,"playsound minecraft:entity.goat.ambient ambient {player} ~ ~ ~ 0.8 0.9")])
    add("mountain_outlaws", "Mountain Outlaws", False, 60, "Outlaws drop from the ridge line.", "minecraft:overworld", "minecraft:jagged_peaks", "DAY", "KILL", 1.1, 0.1,
        [("minecraft:vindicator",2,"Mountain Outlaw",H("minecraft:vindicator","Mountain Outlaw","shadow","minecraft:iron_axe"))], [cmd(0,"playsound minecraft:entity.vindicator.ambient hostile {player} ~ ~ ~ 0.7 0.9")])
    add("cave_glow_spores", "Glow Spore Drift", True, 40, "Glowing spores drift through the passage.", "minecraft:overworld", "minecraft:lush_caves", "BOTH", "TIMED", 0.9, 0.55,
        [("minecraft:bat",2,"Glow Bat",C("Glow Bat","aqua"))], [cmd(0,"playsound minecraft:ambient.cave ambient {player} ~ ~ ~ 0.8 1.0")])
    add("dripstone_cave_robbers", "Dripstone Cave Robbers", False, 60, "Robbers rattle lanterns beneath the stone teeth.", "minecraft:overworld", "minecraft:dripstone_caves", "BOTH", "KILL", 1.1, 0.12,
        [("minecraft:zombie",2,"Cave Robber",H("minecraft:zombie","Cave Robber","shadow","minecraft:iron_sword"))], [cmd(0,"playsound minecraft:ambient.cave hostile {player} ~ ~ ~ 0.8 0.8")])
    add("deep_dark_heartbeat", "Deep Dark Heartbeat", False, 70, "Something below knows your name.", "minecraft:overworld", "minecraft:deep_dark", "BOTH", "KILL", 0.8, 0.05,
        [("minecraft:skeleton",2,"Sculk Revenant",H("minecraft:skeleton","Sculk Revenant","shadow","minecraft:bow",glowing=True,health=28))], [cmd(0,"playsound minecraft:entity.warden.heartbeat hostile {player} ~ ~ ~ 0.9 0.7")])

    add("beach_reef_pirates", "Reef Pirates", False, 60, "Pirates row from the reef line.", "minecraft:overworld", "minecraft:beach", "BOTH", "KILL", 1.3, 0.1,
        [("minecraft:drowned",3,"Reef Cutthroat",H("minecraft:drowned","Reef Cutthroat","sea","minecraft:trident"))], [cmd(0,"playsound minecraft:entity.drowned.ambient_water hostile {player} ~ ~ ~ 0.9 0.9")])
    add("river_floating_merchant", "Floating Merchant", True, 50, "A floating merchant bumps against the reeds.", "minecraft:overworld", "minecraft:river", "DAY", "TIMED", 1.0, 0.5,
        [("minecraft:wandering_trader",1,"Floating Merchant",T("Floating Merchant", [("minecraft:emerald",1,"minecraft:fishing_rod",1,3),("minecraft:emerald",1,"minecraft:cod",4,8),("minecraft:emerald",2,"minecraft:kelp",12,8)], "aqua"))], [cmd(0,"playsound minecraft:entity.boat.paddle_water ambient {player} ~ ~ ~ 0.8 1.0")])
    add("ocean_bell_drowned", "Moon-Drowned Bells", False, 65, "Bells ring beneath the water.", "minecraft:overworld", "minecraft:ocean", "NIGHT", "KILL", 1.1, 0.08,
        [("minecraft:drowned",4,"Bell-Drowned",H("minecraft:drowned","Bell-Drowned","sea","minecraft:trident"))], [cmd(0,"playsound minecraft:block.bell.use hostile {player} ~ ~ ~ 0.8 0.6")])
    add("warm_ocean_pearl_diver", "Pearl Diver", True, 45, "A pearl diver shares a tale of the current.", "minecraft:overworld", "minecraft:warm_ocean", "DAY", "TIMED", 0.9, 0.55,
        [("minecraft:wandering_trader",1,"Pearl Diver",T("Pearl Diver", [("minecraft:emerald",1,"minecraft:kelp",10,8),("minecraft:emerald",2,"minecraft:nautilus_shell",1,1),("minecraft:emerald",1,"minecraft:seagrass",8,8)], "aqua"))], [cmd(0,"playsound minecraft:block.bubble_column.upwards_inside ambient {player} ~ ~ ~ 0.8 1.1")])
    add("frozen_ocean_icewreck", "Icewreck Crew", False, 60, "An old icewreck crew claws from the floes.", "minecraft:overworld", "minecraft:frozen_ocean", "BOTH", "KILL", 1.0, 0.1,
        [("minecraft:stray",2,"Icewreck Archer",H("minecraft:stray","Icewreck Archer","frost","minecraft:bow")), ("minecraft:drowned",1,"Icewreck Deckhand",H("minecraft:drowned","Icewreck Deckhand","frost","minecraft:trident"))], [cmd(0,"playsound minecraft:block.glass.break hostile {player} ~ ~ ~ 0.8 0.7")])

    add("mushroom_forager", "Mushroom Forager", True, 45, "A red-robed forager sells strange stew stories.", "minecraft:overworld", "minecraft:mushroom_fields", "DAY", "TIMED", 0.9, 0.6,
        [("minecraft:wandering_trader",1,"Mushroom Forager",T("Mushroom Forager", [("minecraft:emerald",1,"minecraft:mushroom_stew",1,8),("minecraft:emerald",1,"minecraft:red_mushroom",5,8),("minecraft:emerald",1,"minecraft:brown_mushroom",5,8)], "red"))], [cmd(0,"playsound minecraft:entity.mooshroom.ambient ambient {player} ~ ~ ~ 0.8 1.1")])
    add("cherry_lantern_thieves", "Cherry Lantern Thieves", False, 55, "Lantern thieves scatter petals in your path.", "minecraft:overworld", "minecraft:cherry_grove", "NIGHT", "KILL", 1.0, 0.12,
        [("minecraft:zombie",2,"Cherry Lantern Thief",H("minecraft:zombie","Cherry Lantern Thief","bandit","minecraft:iron_sword",glowing=True))], [cmd(0,"playsound minecraft:block.cherry_leaves.step hostile {player} ~ ~ ~ 0.8 1.0")])

    add("nether_barbecue", "Nether Barbecue", True, 35, "A nervous roaster tends a tiny furnace-camp.", "minecraft:the_nether", "minecraft:nether_wastes", "BOTH", "TIMED", 0.8, 0.45,
        [("minecraft:wandering_trader",1,"Nether Roaster",T("Nether Roaster", [("minecraft:gold_ingot",1,"minecraft:crimson_fungus",2,6),("minecraft:gold_ingot",1,"minecraft:warped_fungus",2,6),("minecraft:gold_ingot",2,"minecraft:fire_charge",1,4)], "gold"))], [cmd(0,"playsound minecraft:block.fire.ambient ambient {player} ~ ~ ~ 0.8 1.0")])
    add("piglin_gold_patrol", "Piglin Gold Patrol", False, 60, "A gold patrol mistakes you for a thief.", "minecraft:the_nether", "minecraft:crimson_forest", "BOTH", "KILL", 1.2, 0.08,
        [("minecraft:piglin",3,"Gold Patrol",H("minecraft:piglin","Gold Patrol","nether","minecraft:golden_sword"))], [cmd(0,"playsound minecraft:entity.piglin.angry hostile {player} ~ ~ ~ 0.9 0.8")])
    add("soul_valley_revenants", "Soul Valley Revenants", False, 65, "Blue fire reveals old bones.", "minecraft:the_nether", "minecraft:soul_sand_valley", "NIGHT", "KILL", 1.1, 0.06,
        [("minecraft:skeleton",2,"Soul Revenant",H("minecraft:skeleton","Soul Revenant","shadow","minecraft:bow",glowing=True))], [cmd(0,"playsound minecraft:block.soul_sand.place hostile {player} ~ ~ ~ 0.8 0.7")])
    add("basalt_mirror_bandits", "Obsidian Mirror Bandits", False, 65, "Mirror-dark bandits step out of the heat shimmer.", "minecraft:the_nether", "minecraft:basalt_deltas", "BOTH", "KILL", 1.05, 0.08,
        [("minecraft:wither_skeleton",2,"Obsidian Bandit",H("minecraft:wither_skeleton","Obsidian Bandit","nether","minecraft:stone_sword",health=28))], [cmd(0,"playsound minecraft:block.basalt.place hostile {player} ~ ~ ~ 0.85 0.7")])
    add("warped_oracle", "Warped Oracle", True, 45, "A warped oracle listens to the fungus breathe.", "minecraft:the_nether", "minecraft:warped_forest", "BOTH", "TIMED", 0.8, 0.55,
        [("minecraft:enderman",1,"Warped Oracle",H("minecraft:enderman","Warped Oracle","end",glowing=True,health=36))], [cmd(0,"playsound minecraft:entity.enderman.ambient ambient {player} ~ ~ ~ 0.7 1.1")])

    add("end_pearl_scholar", "End Pearl Scholar", True, 45, "A silent scholar studies the islands.", "minecraft:the_end", "minecraft:end_highlands", "BOTH", "TIMED", 0.8, 0.55,
        [("minecraft:wandering_trader",1,"End Pearl Scholar",T("End Pearl Scholar", [("minecraft:emerald",1,"minecraft:chorus_fruit",3,6),("minecraft:emerald",3,"minecraft:ender_pearl",1,3),("minecraft:emerald",1,"minecraft:purple_dye",3,8)], "light_purple"))], [cmd(0,"playsound minecraft:entity.enderman.ambient ambient {player} ~ ~ ~ 0.7 1.1")])
    add("end_barrens_hunters", "End Barrens Hunters", False, 70, "Hunters flicker between the stones.", "minecraft:the_end", "minecraft:end_barrens", "NIGHT", "KILL", 1.0, 0.05,
        [("minecraft:enderman",3,"Barrens Hunter",H("minecraft:enderman","Barrens Hunter","end",glowing=True,health=36))], [cmd(0,"playsound minecraft:entity.enderman.stare hostile {player} ~ ~ ~ 0.9 0.7")])
    add("small_end_voidglass_duelists", "Voidglass Duelists", False, 75, "Voidglass duelists bow once, then vanish between strikes.", "minecraft:the_end", "minecraft:small_end_islands", "BOTH", "KILL", 0.35, 0.03,
        [("minecraft:enderman",2,"Voidglass Duelist",H("minecraft:enderman","Voidglass Duelist","end",glowing=True,health=40))], [cmd(0,"playsound minecraft:entity.enderman.teleport hostile {player} ~ ~ ~ 0.8 1.3"), cmd(999,"give {player} minecraft:chorus_fruit 2")])

    add("rare_royal_relic_peddler", "Royal Relic Peddler", True, 45, "A velvet-cloaked peddler opens a box of minor relics.", "minecraft:overworld", "", "BOTH", "TIMED", 0.10, 0.70,
        [("minecraft:wandering_trader",1,"Royal Relic Peddler",T("Royal Relic Peddler", [("minecraft:emerald",3,"minecraft:clock",1,3),("minecraft:emerald",2,"minecraft:compass",1,3),("minecraft:emerald",1,"minecraft:paper",6,8)], "gold"))], [cmd(0,"playsound minecraft:entity.wandering_trader.yes ambient {player} ~ ~ ~ 0.8 0.9")])
    add("rare_clockwork_toll_collectors", "Clockwork Toll Collectors", False, 65, "Brass-masked collectors step from the dust and demand payment.", "minecraft:overworld", "", "BOTH", "KILL", 0.08, 0.04,
        [("minecraft:vindicator",2,"Clockwork Collector",H("minecraft:vindicator","Clockwork Collector","gold","minecraft:iron_axe",glowing=True,health=30))], [cmd(0,"playsound minecraft:block.anvil.use hostile {player} ~ ~ ~ 0.7 0.8"), cmd(999,"give {player} minecraft:copper_ingot 3")])
    add("rare_witchlight_apothecary", "Witchlight Apothecary", True, 50, "A green lantern reveals an apothecary with road remedies.", "minecraft:overworld", "", "BOTH", "TIMED", 0.10, 0.65,
        [("minecraft:villager",1,"Witchlight Apothecary",V("Witchlight Apothecary","cleric", [("minecraft:emerald",1,"minecraft:glow_berries",4,6),("minecraft:emerald",2,"minecraft:honey_bottle",1,4),("minecraft:emerald",1,"minecraft:glass_bottle",3,8)], "dark_green"))], [cmd(0,"playsound minecraft:block.brewing_stand.brew ambient {player} ~ ~ ~ 0.8 1.2")])
    add("rare_ember_relic_guard", "Ember Relic Guard", False, 70, "An ember-armored guard mistakes you for a tomb robber.", "minecraft:the_nether", "", "BOTH", "KILL", 0.08, 0.04,
        [("minecraft:piglin",2,"Ember Relic Guard",H("minecraft:piglin","Ember Relic Guard","gold","minecraft:golden_sword",glowing=True,health=30))], [cmd(0,"playsound minecraft:item.firecharge.use hostile {player} ~ ~ ~ 0.8 0.7"), cmd(999,"give {player} minecraft:gold_nugget 5")])

    if include_pale:
        add("pale_garden_creak", "Pale Garden Creak", False, 70, "The pale trees creak without wind.", "minecraft:overworld", "minecraft:pale_garden", "NIGHT", "KILL", 1.0, 0.04,
            [("minecraft:skeleton",2,"Pale Knight",H("minecraft:skeleton","Pale Knight","pale","minecraft:bow",glowing=True,health=28))], [cmd(0,"playsound minecraft:block.wooden_door.open hostile {player} ~ ~ ~ 0.8 0.7")])
        add("pale_garden_moth", "Pale Garden Moth", True, 40, "A pale moth circles your lantern.", "minecraft:overworld", "minecraft:pale_garden", "DAY", "TIMED", 0.8, 0.55,
            [("minecraft:allay",1,"Pale Moth",C("Pale Moth","white"))], [cmd(0,"playsound minecraft:block.amethyst_block.chime ambient {player} ~ ~ ~ 0.7 1.2")])
    return e

HEADER = '''package com.kmdtravel.eventconfig;

import java.util.ArrayList;
import java.util.List;

public final class DefaultEventProfiles {
    public static final String ID = "kmd_events";
    public static final String NAME = "KMD_Events";

    private DefaultEventProfiles() {
    }

    public static EventProfile create() {
        List<EditableTravelEvent> events = new ArrayList<>();
'''

FOOTER = '''        return new EventProfile(ID, NAME, events);
    }

    private static EditableTravelEvent event(String id, boolean enabled, String title, boolean passive, int durationSeconds, String description, String dimension, String biome, EventTimeOfDay timeOfDay, AggressiveCompletion completion, double weight, double avoidChance, List<String> mobs, List<EventCommandStep> commands) {
        return new EditableTravelEvent(id, enabled, title, passive, durationSeconds, description, dimension, biome, timeOfDay, completion, weight, avoidChance, mobs, commands);
    }

    private static EventCommandStep command(int delaySeconds, String command) {
        return new EventCommandStep(command, delaySeconds);
    }
}
'''


def event_call(event):
    completion = "KILL_MOBS" if event["completion"] == "KILL" else event["completion"]
    mobs = "List.of(" + ", ".join(java_str(mob) for mob in event["mobs"]) + ")"
    commands = "List.of(" + ", ".join(f"command({delay}, {java_str(text)})" for delay, text in event["commands"]) + ")"
    return (
        f'        events.add(event({java_str(event["id"])}, true, {java_str(event["title"])}, {str(event["passive"]).lower()}, '
        f'{event["duration"]}, {java_str(event["desc"])}, {java_str(event["dim"])}, {java_str(event["biome"])}, '
        f'EventTimeOfDay.{event["time"]}, AggressiveCompletion.{completion}, {event["weight"]}D, {event["avoid"]}D, {mobs}, {commands}));'
    )


def patch_saved_data(target):
    path = target / "src" / "main" / "java" / "com" / "kmdtravel" / "eventconfig" / "EventProfileSavedData.java"
    if not path.exists():
        return
    text = path.read_text(encoding="utf-8")
    text = text.replace('public static final String DEFAULT_PROFILE_ID = "default";', 'public static final String DEFAULT_PROFILE_ID = DefaultEventProfiles.ID;')
    text = text.replace('profiles.put(DEFAULT_PROFILE_ID, defaultProfile());\n            globalProfile = DEFAULT_PROFILE_ID;', 'replaceLegacyDefaultIfNeeded();\n            profiles.putIfAbsent(DEFAULT_PROFILE_ID, defaultProfile());\n            globalProfile = DEFAULT_PROFILE_ID;')
    if 'data.replaceLegacyDefaultIfNeeded();' not in text:
        text = text.replace('data.importJsonProfiles();', 'data.importJsonProfiles();\n        data.replaceLegacyDefaultIfNeeded();')
    if 'refreshBundledDefaultProfileIfNeeded' not in text:
        old_seed = '''    private void seedBundledDefaultProfile() {
        if (profiles.containsKey(DEFAULT_PROFILE_ID)) {
            return;
        }
        EventProfile bundled = defaultProfile();
        profiles.put(bundled.id(), bundled);
        if (profiles.size() == 1 || globalProfile == null || globalProfile.isBlank()) {
            globalProfile = bundled.id();
        }
        setDirty();
    }
'''
        new_seed = '''    private void seedBundledDefaultProfile() {
        if (profiles.containsKey(DEFAULT_PROFILE_ID)) {
            refreshBundledDefaultProfileIfNeeded();
            return;
        }
        EventProfile bundled = defaultProfile();
        profiles.put(bundled.id(), bundled);
        if (profiles.size() == 1 || globalProfile == null || globalProfile.isBlank()) {
            globalProfile = bundled.id();
        }
        setDirty();
    }

    private void refreshBundledDefaultProfileIfNeeded() {
        EventProfile existing = profiles.get(DEFAULT_PROFILE_ID);
        if (!isBundledDefaultProfile(existing)) {
            return;
        }
        EventProfile bundled = defaultProfile();
        if (!existing.equals(bundled)) {
            profiles.put(bundled.id(), bundled);
            setDirty();
        }
    }

    private static boolean isBundledDefaultProfile(EventProfile profile) {
        return profile != null && DEFAULT_PROFILE_ID.equals(profile.id()) && DefaultEventProfiles.NAME.equals(profile.name());
    }
'''
        if old_seed in text:
            text = text.replace(old_seed, new_seed)
    pattern = re.compile(r'    private static EventProfile defaultProfile\(\) \{.*?    private static String titleFromKey\(String key\) \{.*?    \}\n', re.S)
    repl = '''    private void replaceLegacyDefaultIfNeeded() {
        EventProfile legacy = profiles.get("default");
        if (legacy == null || profiles.size() != 1 || !isUntouchedLegacyDefault(legacy)) {
            return;
        }
        profiles.clear();
        EventProfile replacement = DefaultEventProfiles.create();
        profiles.put(replacement.id(), replacement);
        globalProfile = replacement.id();
        EventProfileJsonStore.deleteProfile("default");
        setDirty();
    }

    private static boolean isUntouchedLegacyDefault(EventProfile profile) {
        if (!"default".equals(profile.id()) || !("Default".equals(profile.name()) || "default".equals(profile.name()))) {
            return false;
        }
        if (profile.events().isEmpty()) {
            return true;
        }
        for (EditableTravelEvent event : profile.events()) {
            if (!event.dimension().isBlank() || !event.biome().isBlank() || !event.commands().isEmpty() || !event.description().startsWith("event.kmdtravel.")) {
                return false;
            }
        }
        return true;
    }

    private static EventProfile defaultProfile() {
        return DefaultEventProfiles.create();
    }
'''
    new_text = pattern.sub(repl, text)
    new_text = re.sub(r'(    private static EventProfile defaultProfile\(\) \{\n        return DefaultEventProfiles\.create\(\);\n    \}\n)\s*builder\.append\(part\.substring\(0, 1\).*?return builder\.toString\(\);\n    \}\n', r'\1', new_text, flags=re.S)
    if new_text != text:
        path.write_text(new_text, encoding="utf-8")


def generate(target):
    modern = "Forge 1.20.1" not in str(target)
    include_pale = any(marker in str(target) for marker in ("1.21.10", "1.21.11", "26.2"))
    events = make_events(modern, include_pale)
    outdir = target / "src" / "main" / "java" / "com" / "kmdtravel" / "eventconfig"
    outdir.mkdir(parents=True, exist_ok=True)
    body = [HEADER]
    for event in events:
        body.append(event_call(event))
        body.append("")
    body.append(FOOTER)
    (outdir / "DefaultEventProfiles.java").write_text("\n".join(body), encoding="utf-8")
    patch_saved_data(target)
    return events


def write_docs(events):
    lines = [
        "# KMD Default Event Profile",
        "",
        "This page documents the shipped `KMD_Events` profile. It is generated into every loader/version from `generate_default_profiles.py`.",
        "",
        "## NBT Format Notes",
        "",
        "- Forge 1.20.1 uses older item stack NBT inside entity equipment: `Count` and `tag`.",
        "- Minecraft 1.21+ uses component-style item stack data in equipment: `count` and `components`.",
        "- The generator emits the right format per loader, so the same event idea works on each supported version.",
        "- Hostile custom mobs receive weapons or natural hostile AI, persistence, custom names, and themed gear.",
        "- Villagers and wandering traders include `Offers`, so passive merchant stops have actual trades.",
        "",
        "## Event List",
        "",
        "| Event | Type | Dimension | Biome | Time | What happens |",
        "|---|---|---|---|---|---|",
    ]
    for event in events:
        kind = "Passive" if event["passive"] else "Hostile"
        biome = event["biome"] or "Global"
        lines.append(f"| {event['title']} | {kind} | `{event['dim']}` | `{biome}` | {event['time']} | {event['desc']} |")
    lines += [
        "",
        "## Example Highlights",
        "",
        "### Charcoal Burner",
        "A forest passive event that spawns a wandering trader named Charcoal Burner. The trader sells coal, charcoal, and campfires.",
        "",
        "### Toll Road Ruffians",
        "A plains hostile event that spawns armed pillagers with custom names and themed leather gear. It ends when the spawned mobs are killed.",
        "",
        "### Rare Clockwork Toll Collectors",
        "A very low-weight global hostile event with glowing brass-colored vindicators and a small copper reward after completion.",
        "",
        "## Design Rules Used",
        "",
        "- No default event applies harmful potion effects to the player.",
        "- Commands are limited to harmless sounds and small completion rewards.",
        "- Passive merchant events use trades instead of plain villagers.",
        "- Hostile events spawn close to the player by default so encounters start quickly.",
        "- Events are biome-targeted again, with a few rare global events for surprise variety.",
    ]
    (ROOT / "events-reference.md").write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    first_events = None
    for target in TARGETS:
        if (target / "src" / "main" / "java").exists():
            events = generate(target)
            first_events = first_events or events
            print(f"updated {target}")
    if first_events:
        write_docs(first_events)
        print(f"documented {len(first_events)} default events")


if __name__ == "__main__":
    main()
