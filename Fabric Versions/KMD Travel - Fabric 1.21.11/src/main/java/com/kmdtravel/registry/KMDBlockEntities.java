package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class KMDBlockEntities {
    public static final Holder<BlockEntityType<FastTravelPostBlockEntity>> FAST_TRAVEL_POST = holder(
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "fast_travel_post"),
                    FabricBlockEntityTypeBuilder.create(FastTravelPostBlockEntity::new, KMDBlocks.fastTravelPostBlocks()).build()));

    private KMDBlockEntities() {
    }

    private static <T> Holder<T> holder(T value) {
        return new Holder<>(value);
    }

    public static void register() {
    }

    public record Holder<T>(T get) {
    }
}
