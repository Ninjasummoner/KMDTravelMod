package com.kmdtravel.travel;

import net.minecraft.network.chat.Component;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public enum TravelEventKind {
    BANDIT_AMBUSH("bandit_ambush", List.of(EntityType.PILLAGER, EntityType.ZOMBIE), false),
    NIGHT_ATTACK("night_attack", List.of(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER), false),
    FOREST_AMBUSH("forest_ambush", List.of(EntityType.SPIDER, EntityType.SKELETON), false),
    SWAMP_TROUBLE("swamp_trouble", List.of(EntityType.SLIME, EntityType.ZOMBIE), false),
    DESERT_RAIDERS("desert_raiders", List.of(EntityType.HUSK, EntityType.SKELETON), false),
    ROADBLOCK("roadblock", List.of(EntityType.PILLAGER, EntityType.VINDICATOR), false),
    ILLAGER_PATROL("illager_patrol", List.of(EntityType.PILLAGER, EntityType.VINDICATOR), false),
    CAVE_ROBBERS("cave_robbers", List.of(EntityType.ZOMBIE, EntityType.SKELETON), false),
    WOLF_PACK("wolf_pack", List.of(EntityType.WOLF), false),
    LOST_TRAVELER("lost_traveler", List.of(), false),
    ROADSIDE_MERCHANT("roadside_merchant", List.of(), false),
    CAMPFIRE_TRAVELERS("campfire_travelers", List.of(), false),
    HERBALIST("herbalist", List.of(), false),
    BROKEN_CART("broken_cart", List.of(), false),
    SEA_PIRATES("sea_pirates", List.of(EntityType.DROWNED, EntityType.PILLAGER), true),
    DROWNED_RAID("drowned_raid", List.of(EntityType.DROWNED), true),
    SHIPWRECKED_SAILOR("shipwrecked_sailor", List.of(), true),
    FLOATING_MERCHANT("floating_merchant", List.of(), true),
    MESSAGE_IN_A_BOTTLE("message_in_a_bottle", List.of(), true);

    private final String key;
    private final List<EntityType<? extends Mob>> mobs;
    private final boolean seaEvent;

    TravelEventKind(String key, List<EntityType<? extends Mob>> mobs, boolean seaEvent) {
        this.key = key;
        this.mobs = mobs;
        this.seaEvent = seaEvent;
    }

    public List<EntityType<? extends Mob>> mobs() {
        return mobs;
    }

    public Component message() {
        return Component.translatable("event.kmdtravel." + key);
    }

    public Component mobName() {
        return Component.translatable("event.kmdtravel." + key + ".mob");
    }

    public static TravelEventKind byKey(String key) {
        for (TravelEventKind kind : values()) {
            if (kind.key.equals(key)) {
                return kind;
            }
        }
        return BANDIT_AMBUSH;
    }

    public String key() {
        return key;
    }

    public boolean isPeaceful() {
        return mobs.isEmpty();
    }

    public boolean isSeaEvent() {
        return seaEvent;
    }

    public static TravelEventKind pick(Holder<Biome> biome, boolean night, double random, boolean atSea) {
        if (atSea) {
            if (random > 0.72D) {
                return random > 0.90D ? FLOATING_MERCHANT : random > 0.82D ? SHIPWRECKED_SAILOR : MESSAGE_IN_A_BOTTLE;
            }
            return random < 0.55D ? SEA_PIRATES : DROWNED_RAID;
        }
        if (night && random < 0.35D) {
            return NIGHT_ATTACK;
        }
        if (random > 0.78D) {
            if (random > 0.95D) {
                return BROKEN_CART;
            }
            if (random > 0.90D) {
                return HERBALIST;
            }
            if (random > 0.84D) {
                return CAMPFIRE_TRAVELERS;
            }
            return random > 0.81D ? ROADSIDE_MERCHANT : LOST_TRAVELER;
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_JUNGLE)) {
            return random < 0.55D ? FOREST_AMBUSH : WOLF_PACK;
        }
        if (biome.is(BiomeTags.IS_BADLANDS)
                || biome.unwrapKey().filter(key -> key == Biomes.DESERT || key == Biomes.SAVANNA || key == Biomes.SAVANNA_PLATEAU).isPresent()) {
            return DESERT_RAIDERS;
        }
        if (biome.unwrapKey().filter(key -> key == Biomes.SWAMP || key == Biomes.MANGROVE_SWAMP).isPresent()) {
            return SWAMP_TROUBLE;
        }
        if (random < 0.25D) {
            return CAVE_ROBBERS;
        }
        if (random < 0.55D) {
            return BANDIT_AMBUSH;
        }
        return random < 0.78D ? ROADBLOCK : ILLAGER_PATROL;
    }
}
