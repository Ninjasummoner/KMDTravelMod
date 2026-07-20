package com.kmdtravel.fabric;

import com.kmdtravel.block.FastTravelPostRenderer;
import com.kmdtravel.client.ClientMapCache;
import com.kmdtravel.client.KMDClientEvents;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.registry.KMDBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import com.kmdtravel.client.render.TravelMapModel;

public final class KMDTravelFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientMapCache.init();
        KMDNetwork.registerClient();
        KMDClientEvents.register();
        BlockEntityRendererRegistry.register(KMDBlockEntities.FAST_TRAVEL_POST.get(), FastTravelPostRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(TravelMapModel.LAYER, TravelMapModel::createLayer);
    }
}
