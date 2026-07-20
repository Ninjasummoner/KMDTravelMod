package com.kmdtravel.client;

import com.kmdtravel.client.render.TravelMapHandRenderer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class KMDClientEvents {
    private KMDClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        ClientMapCache.tick();
        TravelMapHandRenderer.tick(event);
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        TravelMapHandRenderer.renderHand(event);
    }
}
