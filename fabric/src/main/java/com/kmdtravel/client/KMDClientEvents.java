package com.kmdtravel.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class KMDClientEvents {
    private KMDClientEvents() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientMapCache.tick());
    }
}
