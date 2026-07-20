package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import com.kmdtravel.item.TravelPostBlockItem;
import com.kmdtravel.item.TravelMapItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class KMDItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, KMDTravel.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KMDTravel.MOD_ID);

    public static final DeferredHolder<Item, Item> FAST_TRAVEL_POST = ITEMS.register("fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SPRUCE_FAST_TRAVEL_POST = ITEMS.register("spruce_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.SPRUCE_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> BIRCH_FAST_TRAVEL_POST = ITEMS.register("birch_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.BIRCH_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> JUNGLE_FAST_TRAVEL_POST = ITEMS.register("jungle_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.JUNGLE_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> ACACIA_FAST_TRAVEL_POST = ITEMS.register("acacia_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.ACACIA_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> DARK_OAK_FAST_TRAVEL_POST = ITEMS.register("dark_oak_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.DARK_OAK_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANGROVE_FAST_TRAVEL_POST = ITEMS.register("mangrove_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.MANGROVE_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> CHERRY_FAST_TRAVEL_POST = ITEMS.register("cherry_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.CHERRY_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> SHARED_FAST_TRAVEL_POST = ITEMS.register("shared_fast_travel_post",
            () -> new TravelPostBlockItem(KMDBlocks.SHARED_FAST_TRAVEL_POST.get(), new Item.Properties()));
    public static final DeferredHolder<Item, Item> TRAVEL_MAP = ITEMS.register("travel_map",
            () -> new TravelMapItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KMD_TRAVEL_TAB = CREATIVE_TABS.register("kmd_travel",
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
