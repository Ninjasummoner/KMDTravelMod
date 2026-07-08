package com.kmdtravel.eventconfig;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.travel.TravelEventKind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EventProfileSavedData extends SavedData {
    private static final String NAME = KMDTravel.MOD_ID + "_event_profiles";
    public static final String DEFAULT_PROFILE_ID = "default";

    private final Map<String, EventProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, String> playerProfiles = new LinkedHashMap<>();
    private String globalProfile = DEFAULT_PROFILE_ID;

    public EventProfileSavedData() {
        this(true);
    }

    private EventProfileSavedData(boolean seedDefaultProfile) {
        if (seedDefaultProfile) {
            importJsonProfiles();
            if (profiles.isEmpty()) {
                profiles.put(DEFAULT_PROFILE_ID, defaultProfile());
                globalProfile = DEFAULT_PROFILE_ID;
            }
        } else {
            importJsonProfiles();
        }
        EventProfileJsonStore.writeProfiles(profiles.values());
    }

    public static EventProfileSavedData get(ServerLevel level) {
        EventProfileJsonStore.useWorld(level);
        return level.getServer().overworld().getDataStorage().computeIfAbsent(EventProfileSavedData::load, EventProfileSavedData::new, NAME);
    }

    public static EventProfileSavedData load(CompoundTag tag) {
        EventProfileSavedData data = new EventProfileSavedData(false);
        data.profiles.clear();
        data.globalProfile = tag.getString("GlobalProfile");
        ListTag profilesTag = tag.getList("Profiles", 10);
        for (int i = 0; i < profilesTag.size(); i++) {
            EventProfile profile = EventProfile.load(profilesTag.getCompound(i));
            data.profiles.put(profile.id(), profile);
        }
        ListTag playersTag = tag.getList("PlayerProfiles", 10);
        for (int i = 0; i < playersTag.size(); i++) {
            CompoundTag playerTag = playersTag.getCompound(i);
            if (playerTag.hasUUID("Player")) {
                data.playerProfiles.put(playerTag.getUUID("Player"), playerTag.getString("Profile"));
            }
        }
        if (!data.profiles.isEmpty() && !data.profiles.containsKey(data.globalProfile)) {
            data.globalProfile = data.profiles.values().iterator().next().id();
        } else if (data.profiles.isEmpty()) {
            data.globalProfile = "";
        }
        data.importJsonProfiles();
        EventProfileJsonStore.writeProfiles(data.profiles.values());
        return data;
    }

    public List<EventProfile> profiles() {
        return List.copyOf(profiles.values());
    }

    public Optional<EventProfile> profile(String id) {
        return Optional.ofNullable(profiles.get(id));
    }

    public EventProfile activeProfile(ServerPlayer player) {
        if (profiles.isEmpty()) {
            return null;
        }
        String profileId = playerProfiles.getOrDefault(player.getUUID(), globalProfile);
        return profiles.getOrDefault(profileId, profiles.values().iterator().next());
    }

    public String globalProfile() {
        return globalProfile;
    }

    public void setGlobalProfile(String profileId) {
        if (profiles.containsKey(profileId)) {
            globalProfile = profileId;
            setDirty();
        }
    }

    public String playerProfile(ServerPlayer player) {
        return playerProfiles.getOrDefault(player.getUUID(), globalProfile);
    }

    public void setPlayerProfile(UUID playerId, String profileId) {
        if (profiles.containsKey(profileId)) {
            playerProfiles.put(playerId, profileId);
            setDirty();
        }
    }

    public EventProfile createProfile(String id, String name) {
        String cleanId = cleanId(id);
        if (cleanId.isBlank()) {
            cleanId = "profile_" + (profiles.size() + 1);
        }
        EventProfile profile = profiles.computeIfAbsent(cleanId, key -> new EventProfile(key, name.isBlank() ? key : name, List.of()));
        setDirty();
        return profile;
    }

    public void upsertProfile(EventProfile profile) {
        profiles.put(profile.id(), profile);
        EventProfileJsonStore.writeProfile(profile);
        setDirty();
    }

    public void removeProfile(String profileId) {
        profiles.remove(profileId);
        EventProfileJsonStore.deleteProfile(profileId);
        playerProfiles.entrySet().removeIf(entry -> entry.getValue().equals(profileId));
        if (globalProfile.equals(profileId)) {
            globalProfile = profiles.isEmpty() ? "" : profiles.values().iterator().next().id();
        }
        setDirty();
    }

    private void importJsonProfiles() {
        for (EventProfile profile : EventProfileJsonStore.loadProfiles()) {
            profiles.put(profile.id(), profile);
        }
        if (!profiles.containsKey(globalProfile)) {
            globalProfile = profiles.isEmpty() ? "" : profiles.values().iterator().next().id();
        }
    }

    public CompoundTag saveClientTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("GlobalProfile", globalProfile);
        ListTag profilesTag = new ListTag();
        for (EventProfile profile : profiles.values()) {
            profilesTag.add(profile.save());
        }
        tag.put("Profiles", profilesTag);
        return tag;
    }

    public Optional<EditableTravelEvent> pickEvent(ServerPlayer player, ResourceLocation dimension, List<ResourceLocation> biomes, boolean day, boolean atSea, double random) {
        EventProfile profile = activeProfile(player);
        if (profile == null) {
            return Optional.empty();
        }
        List<EditableTravelEvent> matches = profile.events().stream()
                .filter(event -> biomes.stream().anyMatch(biome -> event.matches(dimension, biome, day)))
                .toList();
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        double total = matches.stream().mapToDouble(event -> Math.max(0.0D, event.weight())).sum();
        if (total <= 0.0D) {
            return Optional.empty();
        }
        double roll = random * total;
        for (EditableTravelEvent event : matches) {
            roll -= Math.max(0.0D, event.weight());
            if (roll <= 0.0D) {
                return Optional.of(event);
            }
        }
        return Optional.of(matches.get(matches.size() - 1));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putString("GlobalProfile", globalProfile);
        ListTag profilesTag = new ListTag();
        for (EventProfile profile : profiles.values()) {
            profilesTag.add(profile.save());
        }
        tag.put("Profiles", profilesTag);
        ListTag playersTag = new ListTag();
        for (Map.Entry<UUID, String> entry : playerProfiles.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            playerTag.putString("Profile", entry.getValue());
            playersTag.add(playerTag);
        }
        tag.put("PlayerProfiles", playersTag);
        return tag;
    }

    private static EventProfile defaultProfile() {
        List<EditableTravelEvent> events = new ArrayList<>();
        for (TravelEventKind kind : TravelEventKind.values()) {
            List<String> mobs = new ArrayList<>();
            for (EntityType<? extends Mob> mob : kind.mobs()) {
                mobs.add(EntityType.getKey(mob).toString());
            }
            events.add(new EditableTravelEvent(
                    kind.key(),
                    true,
                    titleFromKey(kind.key()),
                    kind.isPeaceful(),
                    0,
                    "event.kmdtravel." + kind.key(),
                    "",
                    "",
                    EventTimeOfDay.BOTH,
                    AggressiveCompletion.KILL_MOBS,
                    1.0D,
                    0.0D,
                    List.copyOf(mobs),
                    List.of()));
        }
        return new EventProfile(DEFAULT_PROFILE_ID, "Default", events);
    }

    private static String titleFromKey(String key) {
        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
        }
        return builder.toString();
    }

    private static String cleanId(String id) {
        return id.toLowerCase().replaceAll("[^a-z0-9_/-]", "_");
    }
}


