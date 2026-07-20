package com.kmdtravel.item;

import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.network.OpenTravelScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class TravelMapItem extends Item {
    public TravelMapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            KMDNetwork.sendToPlayer(serverPlayer, OpenTravelScreenPacket.fromPlayerPosition(serverPlayer));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
