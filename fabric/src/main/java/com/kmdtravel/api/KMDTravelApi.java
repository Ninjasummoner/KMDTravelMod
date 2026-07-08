package com.kmdtravel.api;

import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.data.TravelSavedData;
import com.kmdtravel.travel.FastTravelManager;
import com.kmdtravel.travel.TravelLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Public Java API for KMD Travel addon mods.
 *
 * This class is the preferred entry point for Java-code addons. Avoid calling KMD internals directly
 * unless a future KMD version documents that class as stable API.
 */
public final class KMDTravelApi {
    public static final String MOD_ID = "kmdtravel";
    public static final int API_VERSION = 1;

    private KMDTravelApi() {
    }

    public static void registerTravelListener(KMDTravelEventListener listener) {
        KMDTravelEvents.register(listener);
    }

    public static void unregisterTravelListener(KMDTravelEventListener listener) {
        KMDTravelEvents.unregister(listener);
    }

    public static boolean isTraveling(ServerPlayer player) {
        return player != null && FastTravelManager.isTraveling(player);
    }

    public static void requestTravel(ServerPlayer player, UUID sourceId, UUID destinationId) {
        if (player != null && sourceId != null && destinationId != null) {
            FastTravelManager.requestTravel(player, sourceId, destinationId);
        }
    }

    public static void cancelTravel(ServerPlayer player) {
        if (player != null) {
            FastTravelManager.cancelTravel(player);
        }
    }

    public static void cancelTravelToStart(ServerPlayer player) {
        if (player != null) {
            FastTravelManager.cancelTravelToStart(player);
        }
    }

    public static boolean finishEncounter(ServerPlayer player) {
        return player != null && FastTravelManager.finishEncounter(player);
    }

    public static List<TravelLocationView> getTravelLocations(ServerLevel level) {
        if (level == null) {
            return List.of();
        }
        return TravelSavedData.get(level).all().stream()
                .map(TravelLocationView::from)
                .toList();
    }

    public static Optional<TravelLocationView> getTravelLocation(ServerLevel level, UUID id) {
        if (level == null || id == null) {
            return Optional.empty();
        }
        return TravelSavedData.get(level).get(id).map(TravelLocationView::from);
    }

    public static Optional<UUID> findTravelLocationIdByName(ServerLevel level, String name) {
        if (level == null || name == null || name.isBlank()) {
            return Optional.empty();
        }
        return TravelSavedData.get(level).all().stream()
                .filter(location -> location.name().equalsIgnoreCase(name))
                .map(TravelLocation::id)
                .findFirst();
    }

    public static boolean hasDiscovered(ServerPlayer player, UUID locationId) {
        return player != null && locationId != null && PlayerTravelData.hasDiscovered(player, locationId);
    }

    public static boolean discoverLocation(ServerPlayer player, UUID locationId) {
        return player != null && locationId != null && PlayerTravelData.discover(player, locationId);
    }

    public static Set<UUID> getDiscoveredLocationIds(ServerPlayer player) {
        if (player == null) {
            return Set.of();
        }
        return Set.copyOf(PlayerTravelData.discovered(player));
    }

    public static int previewAmbushChance(ServerPlayer player, UUID sourceId, UUID destinationId) {
        if (player == null || sourceId == null || destinationId == null) {
            return 0;
        }
        TravelSavedData savedData = TravelSavedData.get((ServerLevel) player.level());
        Optional<TravelLocation> source = savedData.get(sourceId);
        Optional<TravelLocation> destination = savedData.get(destinationId);
        if (source.isEmpty() || destination.isEmpty()) {
            return 0;
        }
        return FastTravelManager.previewAmbushChance(player, source.get(), destination.get());
    }
}