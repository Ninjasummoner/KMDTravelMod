package com.kmdtravel.api;

import com.kmdtravel.travel.RuntimeTravelEvent;
import net.minecraft.core.BlockPos;

/**
 * Stable read-only snapshot of a KMD travel encounter.
 */
public record TravelEventView(
        String id,
        String title,
        String description,
        boolean passive,
        boolean seaEvent,
        boolean custom,
        int durationSeconds,
        double avoidChance,
        BlockPos encounterPos) {
    static TravelEventView from(RuntimeTravelEvent event, BlockPos encounterPos) {
        if (event == null) {
            return null;
        }
        return new TravelEventView(
                event.id(),
                event.title().getString(),
                event.description().getString(),
                event.passive(),
                event.seaEvent(),
                event.custom(),
                event.durationSeconds(),
                event.avoidChance(),
                encounterPos);
    }
}