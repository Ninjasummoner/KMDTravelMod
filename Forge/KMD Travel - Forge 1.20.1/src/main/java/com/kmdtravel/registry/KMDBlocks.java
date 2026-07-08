package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public final class KMDBlocks {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, KMDTravel.MOD_ID);

    public static final RegistryObject<Block> FAST_TRAVEL_POST = registerPost("fast_travel_post");
    public static final RegistryObject<Block> SPRUCE_FAST_TRAVEL_POST = registerPost("spruce_fast_travel_post");
    public static final RegistryObject<Block> BIRCH_FAST_TRAVEL_POST = registerPost("birch_fast_travel_post");
    public static final RegistryObject<Block> JUNGLE_FAST_TRAVEL_POST = registerPost("jungle_fast_travel_post");
    public static final RegistryObject<Block> ACACIA_FAST_TRAVEL_POST = registerPost("acacia_fast_travel_post");
    public static final RegistryObject<Block> DARK_OAK_FAST_TRAVEL_POST = registerPost("dark_oak_fast_travel_post");
    public static final RegistryObject<Block> MANGROVE_FAST_TRAVEL_POST = registerPost("mangrove_fast_travel_post");
    public static final RegistryObject<Block> CHERRY_FAST_TRAVEL_POST = registerPost("cherry_fast_travel_post");
    public static final RegistryObject<Block> SHARED_FAST_TRAVEL_POST = BLOCKS.register("shared_fast_travel_post",
            () -> new FastTravelPostBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.5F)
                    .sound(SoundType.NETHER_BRICKS)
                    .noOcclusion(), true));

    private static RegistryObject<Block> registerPost(String name) {
        return BLOCKS.register(name,
                () -> new FastTravelPostBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(2.0F)
                        .sound(SoundType.WOOD)
                        .noOcclusion()));
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

    private KMDBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}

