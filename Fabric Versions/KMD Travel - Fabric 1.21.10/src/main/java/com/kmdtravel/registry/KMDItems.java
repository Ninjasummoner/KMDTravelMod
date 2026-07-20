package com.kmdtravel.registry;

import com.kmdtravel.KMDTravel;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.kmdtravel.item.TravelPostBlockItem;
import com.kmdtravel.item.TravelMapItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class KMDItems {
    public static final Holder<Item> FAST_TRAVEL_POST = holder(register("fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> SPRUCE_FAST_TRAVEL_POST = holder(register("spruce_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.SPRUCE_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> BIRCH_FAST_TRAVEL_POST = holder(register("birch_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.BIRCH_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> JUNGLE_FAST_TRAVEL_POST = holder(register("jungle_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.JUNGLE_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> ACACIA_FAST_TRAVEL_POST = holder(register("acacia_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.ACACIA_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> DARK_OAK_FAST_TRAVEL_POST = holder(register("dark_oak_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.DARK_OAK_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> MANGROVE_FAST_TRAVEL_POST = holder(register("mangrove_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.MANGROVE_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> CHERRY_FAST_TRAVEL_POST = holder(register("cherry_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.CHERRY_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> SHARED_FAST_TRAVEL_POST = holder(register("shared_fast_travel_post", id -> new TravelPostBlockItem(KMDBlocks.SHARED_FAST_TRAVEL_POST.get(), itemProperties(id))));
    public static final Holder<Item> TRAVEL_MAP = holder(register("travel_map", id -> new TravelMapItem(itemProperties(id).stacksTo(1))));

    public static final Holder<CreativeModeTab> KMD_TRAVEL_TAB = holder(Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "kmd_travel"),
            FabricItemGroup.builder()
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
                    .build()));

    private KMDItems() {
    }

    private static Item register(String name, java.util.function.Function<ResourceLocation, Item> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, name);
        return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(id));
    }

    private static Item.Properties itemProperties(ResourceLocation id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }

    private static <T> Holder<T> holder(T value) {
        return new Holder<>(value);
    }

    public static void register() {
    }

    public record Holder<T>(T get) {
    }
}
