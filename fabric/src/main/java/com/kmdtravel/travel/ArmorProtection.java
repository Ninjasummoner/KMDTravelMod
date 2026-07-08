package com.kmdtravel.travel;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
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
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) {
            return 0;
        }
        Holder<ArmorMaterial> material = armor.getMaterial();
        if (material == ArmorMaterials.NETHERITE) {
            return 5;
        }
        if (material == ArmorMaterials.DIAMOND) {
            return 4;
        }
        if (material == ArmorMaterials.IRON) {
            return 3;
        }
        if (material == ArmorMaterials.CHAIN || material == ArmorMaterials.GOLD) {
            return 2;
        }
        if (material == ArmorMaterials.LEATHER) {
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
