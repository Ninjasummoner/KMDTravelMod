package com.kmdtravel.eventconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kmdtravel.util.KMDPaths;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;
import java.util.List;

public final class EventProfileJsonStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path activeWorldDirectory = KMDPaths.events().resolve("unknown_world");

    private EventProfileJsonStore() {
    }

    public static void useWorld(ServerLevel level) {
        activeWorldDirectory = KMDPaths.events().resolve(worldFolderName(level));
    }

    public static List<EventProfile> loadProfiles() {
        List<EventProfile> profiles = new ArrayList<>();
        migrateOldDirectory();
        Path directory = directory();
        if (!Files.isDirectory(directory)) {
            return profiles;
        }
        try (var files = Files.list(directory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .forEach(path -> readProfile(path).ifPresent(profiles::add));
        } catch (IOException ignored) {
        }
        return profiles;
    }

    public static void writeProfile(EventProfile profile) {
        try {
            Path directory = directory();
            Files.createDirectories(directory);
            Path path = directory.resolve(cleanFileName(profile.id()) + ".json");
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(toJson(profile), writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static void writeProfiles(Iterable<EventProfile> profiles) {
        for (EventProfile profile : profiles) {
            writeProfile(profile);
        }
    }

    public static void deleteProfile(String profileId) {
        try {
            Files.deleteIfExists(directory().resolve(cleanFileName(profileId) + ".json"));
        } catch (IOException ignored) {
        }
    }

    private static java.util.Optional<EventProfile> readProfile(Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();
            String id = string(object, "id", path.getFileName().toString().replaceFirst("\\.json$", ""));
            String name = string(object, "name", id);
            List<EditableTravelEvent> events = new ArrayList<>();
            JsonArray eventArray = object.has("events") && object.get("events").isJsonArray() ? object.getAsJsonArray("events") : new JsonArray();
            for (JsonElement element : eventArray) {
                if (element.isJsonObject()) {
                    events.add(eventFromJson(element.getAsJsonObject()));
                }
            }
            return java.util.Optional.of(new EventProfile(cleanId(id), name.isBlank() ? id : name, List.copyOf(events)));
        } catch (Exception ignored) {
            return java.util.Optional.empty();
        }
    }

    private static JsonObject toJson(EventProfile profile) {
        JsonObject object = new JsonObject();
        object.addProperty("id", profile.id());
        object.addProperty("name", profile.name());
        JsonArray events = new JsonArray();
        for (EditableTravelEvent event : profile.events()) {
            events.add(eventToJson(event));
        }
        object.add("events", events);
        return object;
    }

    private static JsonObject eventToJson(EditableTravelEvent event) {
        JsonObject object = new JsonObject();
        object.addProperty("id", event.id());
        object.addProperty("enabled", event.enabled());
        object.addProperty("title", event.title());
        object.addProperty("passive", event.passive());
        object.addProperty("durationSeconds", event.passiveDurationSeconds());
        object.addProperty("description", event.description());
        object.addProperty("dimension", event.dimension());
        object.addProperty("biome", event.biome());
        object.addProperty("timeOfDay", event.timeOfDay().name());
        object.addProperty("aggressiveCompletion", event.aggressiveCompletion().name());
        object.addProperty("weight", event.weight());
        object.addProperty("avoidChance", event.avoidChance());
        JsonArray mobs = new JsonArray();
        for (String mob : event.mobs()) {
            mobs.add(mobToJson(mob));
        }
        object.add("mobs", mobs);
        JsonArray commands = new JsonArray();
        for (EventCommandStep command : event.commands()) {
            JsonObject commandObject = new JsonObject();
            commandObject.addProperty("command", command.command());
            commandObject.addProperty("delaySeconds", command.delaySeconds());
            commands.add(commandObject);
        }
        object.add("commands", commands);
        return object;
    }

    private static EditableTravelEvent eventFromJson(JsonObject object) {
        List<String> mobs = new ArrayList<>();
        JsonArray mobArray = object.has("mobs") && object.get("mobs").isJsonArray() ? object.getAsJsonArray("mobs") : new JsonArray();
        for (JsonElement element : mobArray) {
            if (element.isJsonObject()) {
                mobs.add(mobFromJson(element.getAsJsonObject()));
            } else {
                mobs.add(element.getAsString());
            }
        }
        List<EventCommandStep> commands = new ArrayList<>();
        JsonArray commandArray = object.has("commands") && object.get("commands").isJsonArray() ? object.getAsJsonArray("commands") : new JsonArray();
        for (JsonElement element : commandArray) {
            if (element.isJsonObject()) {
                JsonObject command = element.getAsJsonObject();
                commands.add(new EventCommandStep(string(command, "command", ""), integer(command, "delaySeconds", 0)));
            }
        }
        return new EditableTravelEvent(
                cleanId(string(object, "id", "event")),
                bool(object, "enabled", true),
                string(object, "title", "Event"),
                bool(object, "passive", false),
                integer(object, "durationSeconds", 0),
                string(object, "description", ""),
                string(object, "dimension", ""),
                string(object, "biome", ""),
                EventTimeOfDay.byName(string(object, "timeOfDay", "BOTH")),
                AggressiveCompletion.byName(string(object, "aggressiveCompletion", "KILL_MOBS")),
                decimal(object, "weight", 1.0D),
                decimal(object, "avoidChance", 0.0D),
                List.copyOf(mobs),
                List.copyOf(commands));
    }

    private static String string(JsonObject object, String key, String fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
    }

    private static boolean bool(JsonObject object, String key, boolean fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsBoolean() : fallback;
    }

    private static int integer(JsonObject object, String key, int fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsInt() : fallback;
    }

    private static double decimal(JsonObject object, String key, double fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsDouble() : fallback;
    }

    private static String cleanId(String id) {
        return id.toLowerCase().replaceAll("[^a-z0-9_/-]", "_");
    }

    private static String cleanFileName(String id) {
        return cleanId(id).replace('/', '_');
    }

    private static String worldFolderName(ServerLevel level) {
        Path worldPath = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        Path fileName = worldPath.getFileName();
        String displayName = level.getServer().getWorldData().getLevelName();
        String baseName = displayName == null || displayName.isBlank()
                ? fileName == null ? "server" : fileName.toString()
                : displayName;
        String pathKey = Integer.toUnsignedString(worldPath.toString().toLowerCase(Locale.ROOT).hashCode(), 16);
        return cleanFileName(baseName) + "_" + pathKey;
    }

    private static Path directory() {
        return activeWorldDirectory;
    }

    private static void migrateOldDirectory() {
        Path newDirectory = directory();
        Path marker = newDirectory.resolve(".migrated_from_config_events");
        if (Files.exists(marker)) {
            return;
        }
        try {
            Files.createDirectories(newDirectory);
            copyJsonFiles(KMDPaths.events(), newDirectory);
            Files.writeString(marker, "KMD copied global event template JSON files once. Future edits use this world-specific folder.\n", StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static void copyJsonFiles(Path sourceDirectory, Path targetDirectory) {
        if (!Files.isDirectory(sourceDirectory) || sourceDirectory.equals(targetDirectory)) {
            return;
        }
        try (var files = Files.list(sourceDirectory)) {
            files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            Path target = targetDirectory.resolve(path.getFileName());
                            if (!Files.exists(target)) {
                                Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES);
                            }
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private static JsonObject mobToJson(String entry) {
        String[] parts = entry.split("\\|", -1);
        JsonObject object = new JsonObject();
        object.addProperty("mobId", parts.length > 0 ? parts[0] : "");
        object.addProperty("amount", parts.length > 1 ? integer(parts[1], 1) : 1);
        object.addProperty("spawnRange", parts.length > 2 ? integer(parts[2], 12) : 12);
        object.addProperty("name", parts.length > 3 ? decode(parts[3]) : "");
        object.addProperty("nbt", parts.length > 4 ? decode(parts[4]) : "");
        return object;
    }

    private static String mobFromJson(JsonObject object) {
        return string(object, "mobId", "minecraft:zombie")
                + "|" + Math.max(1, Math.min(64, integer(object, "amount", 1)))
                + "|" + Math.max(4, Math.min(96, integer(object, "spawnRange", 12)))
                + "|" + encode(string(object, "name", ""))
                + "|" + encode(string(object, "nbt", ""));
    }

    private static int integer(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static String encode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return "";
        }
    }
}


