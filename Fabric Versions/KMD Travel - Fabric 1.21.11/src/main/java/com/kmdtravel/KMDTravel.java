package com.kmdtravel;

import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.event.KMDCommands;
import com.kmdtravel.event.KMDServerEvents;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.registry.KMDBlockEntities;
import com.kmdtravel.registry.KMDBlocks;
import com.kmdtravel.registry.KMDItems;
import com.kmdtravel.util.KMDPaths;
import com.kmdtravel.util.KMDRecipeExports;
import net.fabricmc.api.ModInitializer;

public class KMDTravel implements ModInitializer {
    public static final String MOD_ID = "kmdtravel";

    @Override
    public void onInitialize() {
        KMDPaths.ensureRoot();
        KMDConfig.load();
        KMDRecipeExports.exportDefaults();
        KMDBlocks.register();
        KMDItems.register();
        KMDBlockEntities.register();
        KMDNetwork.registerServer();
        KMDCommands.register();
        KMDServerEvents.register();
    }
}
