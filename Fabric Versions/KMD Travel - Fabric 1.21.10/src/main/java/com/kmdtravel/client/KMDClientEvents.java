package com.kmdtravel.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.kmdtravel.client.render.TravelMapHandRenderer;

public final class KMDClientEvents {
    private KMDClientEvents() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientMapCache.tick();
            TravelMapHandRenderer.tick();
        });
    }
}
