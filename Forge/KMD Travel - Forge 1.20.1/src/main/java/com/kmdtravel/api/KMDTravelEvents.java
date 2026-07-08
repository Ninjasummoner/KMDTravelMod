package com.kmdtravel.api;

import com.kmdtravel.travel.RuntimeTravelEvent;
import com.kmdtravel.travel.TravelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight event bus used by the public KMD addon API.
 * Addons should register listeners; KMD itself calls the notify methods.
 */
public final class KMDTravelEvents {
    private static final List<KMDTravelEventListener> LISTENERS = new CopyOnWriteArrayList<>();

    private KMDTravelEvents() {
    }

    public static void register(KMDTravelEventListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void unregister(KMDTravelEventListener listener) {
        LISTENERS.remove(listener);
    }

    public static void notifyTravelStarted(ServerPlayer player, TravelLocation source, TravelLocation destination) {
        TravelLocationView sourceView = TravelLocationView.from(source);
        TravelLocationView destinationView = TravelLocationView.from(destination);
        for (KMDTravelEventListener listener : LISTENERS) {
            try {
                listener.onTravelStarted(player, sourceView, destinationView);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static void notifyTravelFinished(ServerPlayer player, TravelLocation destination) {
        TravelLocationView destinationView = TravelLocationView.from(destination);
        for (KMDTravelEventListener listener : LISTENERS) {
            try {
                listener.onTravelFinished(player, destinationView);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static void notifyTravelCancelled(ServerPlayer player, TravelLocation source, TravelLocation destination, boolean returnedToStart) {
        TravelLocationView sourceView = TravelLocationView.from(source);
        TravelLocationView destinationView = TravelLocationView.from(destination);
        for (KMDTravelEventListener listener : LISTENERS) {
            try {
                listener.onTravelCancelled(player, sourceView, destinationView, returnedToStart);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static void notifyEncounterPrompted(ServerPlayer player, RuntimeTravelEvent event, BlockPos encounterPos) {
        TravelEventView eventView = TravelEventView.from(event, encounterPos);
        for (KMDTravelEventListener listener : LISTENERS) {
            try {
                listener.onEncounterPrompted(player, eventView);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static void notifyEncounterStarted(ServerPlayer player, RuntimeTravelEvent event, BlockPos encounterPos) {
        TravelEventView eventView = TravelEventView.from(event, encounterPos);
        for (KMDTravelEventListener listener : LISTENERS) {
            try {
                listener.onEncounterStarted(player, eventView);
            } catch (RuntimeException ignored) {
            }
        }
    }

    public static void notifyEncounterFinished(ServerPlayer player, RuntimeTravelEvent event, BlockPos encounterPos) {
        TravelEventView eventView = TravelEventView.from(event, encounterPos);
        for (KMDTravelEventListener listener : LISTENERS) {
            try {
                listener.onEncounterFinished(player, eventView);
            } catch (RuntimeException ignored) {
            }
        }
    }
}