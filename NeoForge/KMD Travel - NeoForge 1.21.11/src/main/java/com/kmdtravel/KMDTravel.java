package com.kmdtravel;

import com.kmdtravel.block.FastTravelPostRenderer;
import com.kmdtravel.client.ClientMapCache;
import com.kmdtravel.client.KMDClientEvents;
import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.event.KMDCommands;
import com.kmdtravel.event.KMDServerEvents;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.registry.KMDBlockEntities;
import com.kmdtravel.registry.KMDBlocks;
import com.kmdtravel.registry.KMDItems;
import com.kmdtravel.util.KMDPaths;
import com.kmdtravel.util.KMDRecipeExports;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(KMDTravel.MOD_ID)
public class KMDTravel {
    public static final String MOD_ID = "kmdtravel";

    public KMDTravel(IEventBus modBus, ModContainer modContainer) {
        KMDBlocks.register(modBus);
        KMDItems.register(modBus);
        KMDBlockEntities.register(modBus);
        KMDPaths.ensureRoot();
        KMDRecipeExports.exportDefaults();

        modBus.addListener(KMDNetwork::register);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(this::registerRenderers);
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, KMDConfig.SPEC, "kmdtravel/kmdtravel-common.toml");

        NeoForge.EVENT_BUS.register(KMDCommands.class);
        NeoForge.EVENT_BUS.register(KMDServerEvents.class);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientMapCache.init();
            NeoForge.EVENT_BUS.register(KMDClientEvents.class);
        }
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(KMDBlockEntities.FAST_TRAVEL_POST.get(), FastTravelPostRenderer::new);
    }
}


