package com.kmdtravel.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class TravelPostBlockItem extends BlockItem {
    public TravelPostBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kmdtravel.travel_post.open").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.kmdtravel.travel_post.rename").withStyle(ChatFormatting.DARK_GRAY));
    }
}