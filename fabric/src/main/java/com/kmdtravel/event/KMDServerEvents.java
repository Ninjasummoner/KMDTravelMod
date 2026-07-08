package com.kmdtravel.event;

import com.kmdtravel.travel.FastTravelManager;
import com.kmdtravel.data.PlayerTravelData;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class KMDServerEvents {
    private KMDServerEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getAllLevels().forEach(FastTravelManager::tick));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> PlayerTravelData.copy(oldPlayer, newPlayer));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> FastTravelManager.cancelTravelToStart(handler.player));
    }
}
