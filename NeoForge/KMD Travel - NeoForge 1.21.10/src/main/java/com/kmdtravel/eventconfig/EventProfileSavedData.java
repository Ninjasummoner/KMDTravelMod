package com.kmdtravel.eventconfig;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.util.NbtCompat;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EventProfileSavedData extends SavedData {
    private static final String NAME = KMDTravel.MOD_ID + "_event_profiles";
    private static final Codec<EventProfileSavedData> CODEC = CompoundTag.CODEC.xmap(
            tag -> load(tag, null),
            EventProfileSavedData::saveTag);
    private static final SavedDataType<EventProfileSavedData> TYPE = new SavedDataType<>(NAME, EventProfileSavedData::new, CODEC);
    public static final String DEFAULT_PROFILE_ID = DefaultEventProfiles.ID;

    private final Map<String, EventProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, String> playerProfiles = new LinkedHashMap<>();
    private String globalProfile = DEFAULT_PROFILE_ID;

    public EventProfileSavedData() {
        this(true);
    }

    private EventProfileSavedData(boolean seedDefaultProfile) {
        if (seedDefaultProfile) {
            syncProfilesWithJson();
        }
    }

    public static EventProfileSavedData get(ServerLevel level) {
        EventProfileJsonStore.useWorld(level);
        return level.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static EventProfileSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        EventProfileSavedData data = new EventProfileSavedData(false);
        data.profiles.clear();
        data.globalProfile = tag.getStringOr("GlobalProfile", DEFAULT_PROFILE_ID);
        ListTag profilesTag = tag.getListOrEmpty("Profiles");
        for (int i = 0; i < profilesTag.size(); i++) {
            profilesTag.getCompound(i).ifPresent(profileTag -> {
                EventProfile profile = EventProfile.load(profileTag);
                data.profiles.put(profile.id(), profile);
            });
        }
        ListTag playersTag = tag.getListOrEmpty("PlayerProfiles");
        for (int i = 0; i < playersTag.size(); i++) {
            playersTag.getCompound(i).ifPresent(playerTag ->
                    data.playerProfiles.put(NbtCompat.getUuid(playerTag, "Player", UUID.randomUUID()), playerTag.getStringOr("Profile", DEFAULT_PROFILE_ID)));
        }
        data.syncProfilesWithJson();
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

    private void syncProfilesWithJson() {
        importJsonProfiles();
        replaceLegacyDefaultIfNeeded();
        seedBundledDefaultProfile();
        normalizeGlobalProfile();
        EventProfileJsonStore.writeProfiles(profiles.values());
    }

    private void importJsonProfiles() {
        for (EventProfile profile : EventProfileJsonStore.loadProfiles()) {
            profiles.put(profile.id(), profile);
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

    public Optional<EditableTravelEvent> pickEvent(ServerPlayer player, ResourceLocation dimension, List<ResourceLocation> biomes, boolean day, double random) {
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

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return save(tag);
    }

    private CompoundTag saveTag() {
        return save(new CompoundTag());
    }

    private CompoundTag save(CompoundTag tag) {
        tag.putString("GlobalProfile", globalProfile);
        ListTag profilesTag = new ListTag();
        for (EventProfile profile : profiles.values()) {
            profilesTag.add(profile.save());
        }
        tag.put("Profiles", profilesTag);
        ListTag playersTag = new ListTag();
        for (Map.Entry<UUID, String> entry : playerProfiles.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            NbtCompat.putUuid(playerTag, "Player", entry.getKey());
            playerTag.putString("Profile", entry.getValue());
            playersTag.add(playerTag);
        }
        tag.put("PlayerProfiles", playersTag);
        return tag;
    }

    private void seedBundledDefaultProfile() {
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
    private void normalizeGlobalProfile() {
        if (profiles.isEmpty()) {
            globalProfile = "";
            return;
        }
        if (globalProfile == null || globalProfile.isBlank() || !profiles.containsKey(globalProfile)) {
            globalProfile = profiles.values().iterator().next().id();
        }
    }

    private void replaceLegacyDefaultIfNeeded() {
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

    private static String cleanId(String id) {
        return id.toLowerCase().replaceAll("[^a-z0-9_/-]", "_");
    }
}
