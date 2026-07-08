package com.kmdtravel.data;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlockEntity;
import com.kmdtravel.travel.TravelLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TravelSavedData extends SavedData {
    private static final String NAME = KMDTravel.MOD_ID + "_locations";
    private final Map<UUID, TravelLocation> locations = new LinkedHashMap<>();

    public static TravelSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TravelSavedData::new, TravelSavedData::load),
                NAME);
    }

    public static TravelSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        TravelSavedData data = new TravelSavedData();
        ListTag list = tag.getList("Locations", 10);
        for (int i = 0; i < list.size(); i++) {
            TravelLocation location = TravelLocation.load(list.getCompound(i));
            data.locations.put(location.id(), location);
        }
        return data;
    }

    public void upsertFromPost(FastTravelPostBlockEntity post) {
        if (post.getLevel() == null) {
            return;
        }
        TravelLocation location = new TravelLocation(
                post.getLocationId(),
                post.getPostName().getString(),
                post.getLevel().dimension().location(),
                post.getBlockPos(),
                post.isShared(),
                post.getMarkerColor(),
                post.getMarkerPattern());
        locations.put(location.id(), location);
        setDirty();
    }

    public Optional<TravelLocation> get(UUID id) {
        return Optional.ofNullable(locations.get(id));
    }

    public void remove(UUID id) {
        if (locations.remove(id) != null) {
            setDirty();
        }
    }

    public Collection<TravelLocation> all() {
        return locations.values();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (TravelLocation location : locations.values()) {
            list.add(location.save());
        }
        tag.put("Locations", list);
        return tag;
    }
}
