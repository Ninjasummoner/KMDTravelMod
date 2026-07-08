package com.kmdtravel.data;

import com.kmdtravel.KMDTravel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerTravelData {
    private static final String ROOT = KMDTravel.MOD_ID;
    private static final String DISCOVERED = "DiscoveredLocations";

    private PlayerTravelData() {
    }

    public static boolean discover(ServerPlayer player, UUID id) {
        Set<UUID> known = discovered(player);
        boolean added = known.add(id);
        write(player, known);
        return added;
    }

    public static boolean hasDiscovered(ServerPlayer player, UUID id) {
        return discovered(player).contains(id);
    }

    public static void copy(ServerPlayer original, ServerPlayer player) {
        write(player, discovered(original));
    }

    public static Set<UUID> discovered(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompoundOrEmpty(ROOT);
        ListTag list = root.getListOrEmpty(DISCOVERED);
        Set<UUID> ids = new LinkedHashSet<>();
        for (int i = 0; i < list.size(); i++) {
            try {
                ids.add(UUID.fromString(list.getString(i).orElse("")));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return ids;
    }

    private static void write(ServerPlayer player, Set<UUID> ids) {
        CompoundTag root = player.getPersistentData().getCompoundOrEmpty(ROOT);
        ListTag list = new ListTag();
        for (UUID id : ids) {
            list.add(StringTag.valueOf(id.toString()));
        }
        root.put(DISCOVERED, list);
        player.getPersistentData().put(ROOT, root);
    }
}
