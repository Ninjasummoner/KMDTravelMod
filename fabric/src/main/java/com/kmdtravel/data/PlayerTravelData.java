package com.kmdtravel.data;

import com.kmdtravel.KMDTravel;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerTravelData {
    private PlayerTravelData() {
    }

    public static boolean discover(ServerPlayer player, UUID id) {
        Storage storage = Storage.get(player.serverLevel());
        Set<UUID> known = storage.discovered(player.getUUID());
        boolean added = known.add(id);
        if (added) {
            storage.setDiscovered(player.getUUID(), known);
        }
        return added;
    }

    public static boolean hasDiscovered(ServerPlayer player, UUID id) {
        return Storage.get(player.serverLevel()).discovered(player.getUUID()).contains(id);
    }

    public static void copy(ServerPlayer original, ServerPlayer player) {
        Storage storage = Storage.get(player.serverLevel());
        storage.setDiscovered(player.getUUID(), storage.discovered(original.getUUID()));
    }

    public static Set<UUID> discovered(ServerPlayer player) {
        return Storage.get(player.serverLevel()).discovered(player.getUUID());
    }

    private static final class Storage extends SavedData {
        private static final String NAME = KMDTravel.MOD_ID + "_player_travel";
        private final Map<UUID, Set<UUID>> discovered = new LinkedHashMap<>();

        static Storage get(ServerLevel level) {
            return level.getServer().overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(Storage::new, Storage::load, null),
                    NAME);
        }

        static Storage load(CompoundTag tag, HolderLookup.Provider registries) {
            Storage storage = new Storage();
            ListTag players = tag.getList("Players", 10);
            for (int i = 0; i < players.size(); i++) {
                CompoundTag playerTag = players.getCompound(i);
                if (!playerTag.hasUUID("Player")) {
                    continue;
                }
                Set<UUID> locations = new LinkedHashSet<>();
                ListTag list = playerTag.getList("DiscoveredLocations", 8);
                for (int entry = 0; entry < list.size(); entry++) {
                    try {
                        locations.add(UUID.fromString(list.getString(entry)));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                storage.discovered.put(playerTag.getUUID("Player"), locations);
            }
            return storage;
        }

        Set<UUID> discovered(UUID playerId) {
            return new LinkedHashSet<>(discovered.getOrDefault(playerId, Set.of()));
        }

        void setDiscovered(UUID playerId, Set<UUID> ids) {
            discovered.put(playerId, new LinkedHashSet<>(ids));
            setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag players = new ListTag();
            for (Map.Entry<UUID, Set<UUID>> entry : discovered.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                playerTag.putUUID("Player", entry.getKey());
                ListTag list = new ListTag();
                for (UUID id : entry.getValue()) {
                    list.add(StringTag.valueOf(id.toString()));
                }
                playerTag.put("DiscoveredLocations", list);
                players.add(playerTag);
            }
            tag.put("Players", players);
            return tag;
        }
    }
}
