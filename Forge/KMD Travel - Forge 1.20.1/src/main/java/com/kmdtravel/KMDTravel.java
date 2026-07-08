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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(KMDTravel.MOD_ID)
public class KMDTravel {
    public static final String MOD_ID = "kmdtravel";

    public KMDTravel() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        KMDBlocks.register(modBus);
        KMDItems.register(modBus);
        KMDBlockEntities.register(modBus);
        KMDPaths.ensureRoot();
        KMDRecipeExports.exportDefaults();

        modBus.addListener(this::registerRenderers);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, KMDConfig.SPEC, "kmdtravel/kmdtravel-common.toml");
        KMDNetwork.register();

        MinecraftForge.EVENT_BUS.register(KMDCommands.class);
        MinecraftForge.EVENT_BUS.register(KMDServerEvents.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientMapCache.init();
            MinecraftForge.EVENT_BUS.register(KMDClientEvents.class);
        });
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                event.registerBlockEntityRenderer(KMDBlockEntities.FAST_TRAVEL_POST.get(), FastTravelPostRenderer::new));
    }
}

