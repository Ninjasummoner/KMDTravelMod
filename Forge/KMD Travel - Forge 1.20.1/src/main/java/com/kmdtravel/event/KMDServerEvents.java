package com.kmdtravel.event;

import com.kmdtravel.travel.FastTravelManager;
import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.registry.KMDItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class KMDServerEvents {
    private KMDServerEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel serverLevel) {
            FastTravelManager.tick(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!KMDConfig.ENABLE_SHARED_POST_CRAFTING.get()
                && event.getCrafting().is(KMDItems.SHARED_FAST_TRAVEL_POST.get())
                && event.getEntity() instanceof ServerPlayer player) {
            event.getCrafting().setCount(0);
            player.displayClientMessage(Component.translatable("message.kmdtravel.shared_post_crafting_disabled"), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer original && event.getEntity() instanceof ServerPlayer player) {
            PlayerTravelData.copy(original, player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FastTravelManager.cancelTravelToStart(player);
        }
    }
}

