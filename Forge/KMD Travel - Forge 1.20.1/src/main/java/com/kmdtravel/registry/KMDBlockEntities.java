package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class KMDBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, KMDTravel.MOD_ID);

    public static final RegistryObject<BlockEntityType<FastTravelPostBlockEntity>> FAST_TRAVEL_POST =
            BLOCK_ENTITIES.register("fast_travel_post",
                    () -> BlockEntityType.Builder.of(FastTravelPostBlockEntity::new, KMDBlocks.fastTravelPostBlocks()).build(null));

    private KMDBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}

