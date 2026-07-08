package com.kmdtravel.travel;

import com.kmdtravel.eventconfig.EditableTravelEvent;
import com.kmdtravel.eventconfig.EventCommandStep;
import com.kmdtravel.eventconfig.AggressiveCompletion;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public record RuntimeTravelEvent(
        String id,
        Component title,
        Component description,
        boolean passive,
        int durationSeconds,
        boolean seaEvent,
        AggressiveCompletion aggressiveCompletion,
        double avoidChance,
        List<MobSpawnSpec> mobs,
        List<EventCommandStep> commands,
        boolean custom) {
    public record MobSpawnSpec(EntityType<?> type, int range, String name, String nbt) {
    }

    public static RuntimeTravelEvent builtin(TravelEventKind kind, int durationSeconds) {
        List<MobSpawnSpec> mobs = kind.mobs().stream()
                .map(type -> new MobSpawnSpec(type, 12, "", ""))
                .toList();
        return new RuntimeTravelEvent(
                kind.key(),
                kind.mobName(),
                kind.message(),
                kind.isPeaceful(),
                durationSeconds,
                kind.isSeaEvent(),
                AggressiveCompletion.KILL_MOBS,
                0.0D,
                mobs,
                List.of(),
                false);
    }

    public static RuntimeTravelEvent custom(EditableTravelEvent event, int globalDurationSeconds) {
        List<MobSpawnSpec> mobs = new ArrayList<>();
        for (String mobEntry : event.mobs()) {
            String[] parts = mobEntry.split("\\|", -1);
            String mobId = parts.length > 0 ? parts[0] : "";
            ResourceLocation id = ResourceLocation.tryParse(parts[0]);
            if (id == null) {
                continue;
            }
            Optional<EntityType<?>> type = EntityType.byString(id.toString());
            int count = parts.length > 1 ? parseCount(parts[1]) : 1;
            int range = parts.length > 2 ? parseRange(parts[2]) : 12;
            String name = parts.length > 3 ? decode(parts[3]) : "";
            String nbt = parts.length > 4 ? decode(parts[4]) : "";
            type.ifPresent(entityType -> {
                for (int i = 0; i < count; i++) {
                    mobs.add(new MobSpawnSpec(entityType, range, name, nbt));
                }
            });
        }
        int duration = event.passiveDurationSeconds() > 0 ? event.passiveDurationSeconds() : globalDurationSeconds;
        boolean sea = event.id().contains("sea")
                || event.id().contains("drowned")
                || event.id().contains("ship")
                || event.id().contains("floating")
                || event.id().contains("bottle")
                || event.mobs().stream().anyMatch(mob -> mob.contains("drowned") || mob.contains("boat") || mob.contains("pirate"));
        Component description = event.description().startsWith("event.kmdtravel.")
                ? Component.translatable(event.description())
                : Component.literal(event.description().isBlank() ? event.title() : event.description());
        return new RuntimeTravelEvent(
                event.id(),
                Component.literal(event.title().isBlank() ? event.id() : event.title()),
                description,
                event.passive(),
                duration,
                sea,
                event.aggressiveCompletion(),
                event.avoidChance(),
                List.copyOf(mobs),
                event.commands(),
                true);
    }

    private static int parseCount(String value) {
        try {
            return Math.max(1, Math.min(64, Integer.parseInt(value.trim())));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private static int parseRange(String value) {
        try {
            return Math.max(4, Math.min(96, Integer.parseInt(value.trim())));
        } catch (NumberFormatException ignored) {
            return 12;
        }
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
