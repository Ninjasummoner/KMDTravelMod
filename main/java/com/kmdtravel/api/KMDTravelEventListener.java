package com.kmdtravel.api;

import net.minecraft.server.level.ServerPlayer;

/**
 * Listener interface for addon mods that want to react to KMD travel lifecycle events.
 *
 * Register listeners through {@link KMDTravelApi#registerTravelListener(KMDTravelEventListener)}.
 * Keep listener work lightweight; heavy logic should be scheduled by your own mod.
 */
public interface KMDTravelEventListener {
    default void onTravelStarted(ServerPlayer player, TravelLocationView source, TravelLocationView destination) {
    }

    default void onTravelFinished(ServerPlayer player, TravelLocationView destination) {
    }

    default void onTravelCancelled(ServerPlayer player, TravelLocationView source, TravelLocationView destination, boolean returnedToStart) {
    }

    default void onEncounterPrompted(ServerPlayer player, TravelEventView event) {
    }

    default void onEncounterStarted(ServerPlayer player, TravelEventView event) {
    }

    default void onEncounterFinished(ServerPlayer player, TravelEventView event) {
    }
}