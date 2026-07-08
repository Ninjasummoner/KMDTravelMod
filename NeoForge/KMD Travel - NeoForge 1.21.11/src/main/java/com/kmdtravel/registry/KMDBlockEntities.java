package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class KMDBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, KMDTravel.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FastTravelPostBlockEntity>> FAST_TRAVEL_POST =
            BLOCK_ENTITIES.register("fast_travel_post",
                    () -> new BlockEntityType<>(FastTravelPostBlockEntity::new, KMDBlocks.fastTravelPostBlocks()));

    private KMDBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}


