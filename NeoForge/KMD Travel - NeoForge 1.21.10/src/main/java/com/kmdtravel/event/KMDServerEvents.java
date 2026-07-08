package com.kmdtravel.event;

import com.kmdtravel.travel.FastTravelManager;
import com.kmdtravel.config.KMDConfig;
import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.registry.KMDItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class KMDServerEvents {
    private static final Map<UUID, Integer> PENDING_SHARED_POST_REMOVALS = new HashMap<>();

    private KMDServerEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            FastTravelManager.tick(serverLevel);
            removeBlockedSharedPosts(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!KMDConfig.ENABLE_SHARED_POST_CRAFTING.get()
                && event.getCrafting().is(KMDItems.SHARED_FAST_TRAVEL_POST.get())
                && event.getEntity() instanceof ServerPlayer player) {
            int craftedCount = Math.max(1, event.getCrafting().getCount());
            event.getCrafting().setCount(0);
            PENDING_SHARED_POST_REMOVALS.merge(player.getUUID(), craftedCount, Integer::sum);
            player.displayClientMessage(Component.translatable("message.kmdtravel.shared_post_crafting_disabled"), true);
        }
    }

    private static void removeBlockedSharedPosts(ServerLevel level) {
        if (KMDConfig.ENABLE_SHARED_POST_CRAFTING.get() || PENDING_SHARED_POST_REMOVALS.isEmpty()) {
            PENDING_SHARED_POST_REMOVALS.clear();
            return;
        }
        Iterator<Map.Entry<UUID, Integer>> iterator = PENDING_SHARED_POST_REMOVALS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            int remaining = entry.getValue();
            for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.is(KMDItems.SHARED_FAST_TRAVEL_POST.get())) {
                    continue;
                }
                int removed = Math.min(remaining, stack.getCount());
                stack.shrink(removed);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
                remaining -= removed;
            }
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
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
