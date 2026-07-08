package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class KMDBlocks {
    public static final Holder<Block> FAST_TRAVEL_POST = holder(registerPost("fast_travel_post"));
    public static final Holder<Block> SPRUCE_FAST_TRAVEL_POST = holder(registerPost("spruce_fast_travel_post"));
    public static final Holder<Block> BIRCH_FAST_TRAVEL_POST = holder(registerPost("birch_fast_travel_post"));
    public static final Holder<Block> JUNGLE_FAST_TRAVEL_POST = holder(registerPost("jungle_fast_travel_post"));
    public static final Holder<Block> ACACIA_FAST_TRAVEL_POST = holder(registerPost("acacia_fast_travel_post"));
    public static final Holder<Block> DARK_OAK_FAST_TRAVEL_POST = holder(registerPost("dark_oak_fast_travel_post"));
    public static final Holder<Block> MANGROVE_FAST_TRAVEL_POST = holder(registerPost("mangrove_fast_travel_post"));
    public static final Holder<Block> CHERRY_FAST_TRAVEL_POST = holder(registerPost("cherry_fast_travel_post"));
    public static final Holder<Block> SHARED_FAST_TRAVEL_POST = holder(register("shared_fast_travel_post",
            new FastTravelPostBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.5F)
                    .sound(SoundType.NETHER_BRICKS)
                    .noOcclusion(), true)));

    private KMDBlocks() {
    }

    private static Block registerPost(String name) {
        return register(name, new FastTravelPostBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()));
    }

    private static Block register(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, name), block);
    }

    private static <T> Holder<T> holder(T value) {
        return new Holder<>(value);
    }

    public static Block[] fastTravelPostBlocks() {
        return new Block[] {
                FAST_TRAVEL_POST.get(),
                SPRUCE_FAST_TRAVEL_POST.get(),
                BIRCH_FAST_TRAVEL_POST.get(),
                JUNGLE_FAST_TRAVEL_POST.get(),
                ACACIA_FAST_TRAVEL_POST.get(),
                DARK_OAK_FAST_TRAVEL_POST.get(),
                MANGROVE_FAST_TRAVEL_POST.get(),
                CHERRY_FAST_TRAVEL_POST.get(),
                SHARED_FAST_TRAVEL_POST.get()
        };
    }

    public static void register() {
    }

    public record Holder<T>(T get) {
    }
}
