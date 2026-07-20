package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import com.kmdtravel.item.TravelPostBlockItem;
import com.kmdtravel.item.TravelMapItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class KMDItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, KMDTravel.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KMDTravel.MOD_ID);

    public static final RegistryObject<Item> FAST_TRAVEL_POST = ITEMS.register("fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> SPRUCE_FAST_TRAVEL_POST = ITEMS.register("spruce_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.SPRUCE_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> BIRCH_FAST_TRAVEL_POST = ITEMS.register("birch_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.BIRCH_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> JUNGLE_FAST_TRAVEL_POST = ITEMS.register("jungle_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.JUNGLE_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> ACACIA_FAST_TRAVEL_POST = ITEMS.register("acacia_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.ACACIA_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> DARK_OAK_FAST_TRAVEL_POST = ITEMS.register("dark_oak_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.DARK_OAK_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> MANGROVE_FAST_TRAVEL_POST = ITEMS.register("mangrove_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.MANGROVE_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> CHERRY_FAST_TRAVEL_POST = ITEMS.register("cherry_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.CHERRY_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> SHARED_FAST_TRAVEL_POST = ITEMS.register("shared_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.SHARED_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final RegistryObject<Item> TRAVEL_MAP = ITEMS.register("travel_map",
            () -> new TravelMapItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<CreativeModeTab> KMD_TRAVEL_TAB = CREATIVE_TABS.register("kmd_travel",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kmdtravel"))
                    .icon(() -> FAST_TRAVEL_POST.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FAST_TRAVEL_POST.get());
                        output.accept(SPRUCE_FAST_TRAVEL_POST.get());
                        output.accept(BIRCH_FAST_TRAVEL_POST.get());
                        output.accept(JUNGLE_FAST_TRAVEL_POST.get());
                        output.accept(ACACIA_FAST_TRAVEL_POST.get());
                        output.accept(DARK_OAK_FAST_TRAVEL_POST.get());
                        output.accept(MANGROVE_FAST_TRAVEL_POST.get());
                        output.accept(CHERRY_FAST_TRAVEL_POST.get());
                        output.accept(SHARED_FAST_TRAVEL_POST.get());
                        output.accept(TRAVEL_MAP.get());
                    })
                    .build());

    private KMDItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        CREATIVE_TABS.register(bus);
    }
}

