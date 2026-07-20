package com.kmdtravel.api;

import com.kmdtravel.travel.TravelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * Stable read-only snapshot of a KMD travel location.
 */
public record TravelLocationView(
        UUID id,
        String name,
        Identifier dimension,
        BlockPos pos,
        boolean shared,
        int markerColor,
        int markerPattern) {
    static TravelLocationView from(TravelLocation location) {
        return new TravelLocationView(
                location.id(),
                location.name(),
                location.dimension(),
                location.pos(),
                location.shared(),
                location.markerColor(),
                location.markerPattern());
    }
}