package com.kmdtravel.travel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class ArmorProtection {
    private static final double[] TIER_REDUCTION = {0.0D, 0.04D, 0.08D, 0.12D, 0.16D, 0.20D};
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private ArmorProtection() {
    }

    public static int tier(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        String itemId = stack.getItem().toString().toLowerCase();
        if (itemId.contains("netherite")) {
            return 5;
        }
        if (itemId.contains("diamond")) {
            return 4;
        }
        if (itemId.contains("iron")) {
            return 3;
        }
        if (itemId.contains("chain") || itemId.contains("gold")) {
            return 2;
        }
        if (itemId.contains("leather")) {
            return 1;
        }
        return 0;
    }

    public static double eventReduction(ServerPlayer player) {
        double armorPoints = player.getAttributeValue(Attributes.ARMOR);
        double toughness = player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        double reduction = (armorPoints + toughness * 0.5D) * com.kmdtravel.config.KMDConfig.ARMOR_SAFETY_PER_POINT.get();
        return Math.min(com.kmdtravel.config.KMDConfig.MAX_ARMOR_EVENT_REDUCTION.get(), Math.max(0.0D, reduction));
    }

    public static int skipChancePercent(ServerPlayer player) {
        return (int) Math.round(Math.min(0.90D, 0.20D + eventReduction(player)) * 100.0D);
    }

    private static double legacyTierReduction(ServerPlayer player) {
        int bestTier = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            bestTier = Math.max(bestTier, tier(player.getItemBySlot(slot)));
        }
        if (bestTier < 0 || bestTier >= TIER_REDUCTION.length) {
            return 0.0D;
        }
        return TIER_REDUCTION[bestTier];
    }
}


