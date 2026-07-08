package com.kmdtravel.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class KMDClientEvents {
    private KMDClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        ClientMapCache.tick();
    }
}
