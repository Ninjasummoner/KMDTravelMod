package com.kmdtravel.data;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.util.NbtCompat;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerTravelData {
    private PlayerTravelData() {
    }

    public static boolean discover(ServerPlayer player, UUID id) {
        Storage storage = Storage.get(((ServerLevel) player.level()));
        Set<UUID> known = storage.discovered(player.getUUID());
        boolean added = known.add(id);
        if (added) {
            storage.setDiscovered(player.getUUID(), known);
        }
        return added;
    }

    public static boolean hasDiscovered(ServerPlayer player, UUID id) {
        return Storage.get(((ServerLevel) player.level())).discovered(player.getUUID()).contains(id);
    }

    public static void copy(ServerPlayer original, ServerPlayer player) {
        Storage storage = Storage.get(((ServerLevel) player.level()));
        storage.setDiscovered(player.getUUID(), storage.discovered(original.getUUID()));
    }

    public static Set<UUID> discovered(ServerPlayer player) {
        return Storage.get(((ServerLevel) player.level())).discovered(player.getUUID());
    }

    private static final class Storage extends SavedData {
        private static final String NAME = KMDTravel.MOD_ID + "_player_travel";
        private static final Codec<Storage> CODEC = CompoundTag.CODEC.xmap(
                tag -> load(tag, null),
                Storage::saveTag);
        private static final SavedDataType<Storage> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "player_travel"),
                Storage::new,
                CODEC,
                DataFixTypes.LEVEL);
        private final Map<UUID, Set<UUID>> discovered = new LinkedHashMap<>();

        static Storage get(ServerLevel level) {
            return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
        }

        static Storage load(CompoundTag tag, HolderLookup.Provider registries) {
            Storage storage = new Storage();
            ListTag players = tag.getListOrEmpty("Players");
            for (int i = 0; i < players.size(); i++) {
                CompoundTag playerTag = players.getCompound(i).orElse(null);
                if (playerTag == null) {
                    continue;
                }
                Set<UUID> locations = new LinkedHashSet<>();
                ListTag list = playerTag.getListOrEmpty("DiscoveredLocations");
                for (int entry = 0; entry < list.size(); entry++) {
                    try {
                        locations.add(UUID.fromString(list.getString(entry).orElse("")));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                storage.discovered.put(NbtCompat.getUuid(playerTag, "Player", UUID.randomUUID()), locations);
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

        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            return save(tag);
        }

        private CompoundTag saveTag() {
            return save(new CompoundTag());
        }

        private CompoundTag save(CompoundTag tag) {
            ListTag players = new ListTag();
            for (Map.Entry<UUID, Set<UUID>> entry : discovered.entrySet()) {
                CompoundTag playerTag = new CompoundTag();
                NbtCompat.putUuid(playerTag, "Player", entry.getKey());
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
