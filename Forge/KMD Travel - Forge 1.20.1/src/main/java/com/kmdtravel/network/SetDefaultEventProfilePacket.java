package com.kmdtravel.network;

import com.kmdtravel.eventconfig.EventProfileSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetDefaultEventProfilePacket(String profileId) {
    public static void encode(SetDefaultEventProfilePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.profileId(), 64);
    }

    public static SetDefaultEventProfilePacket decode(FriendlyByteBuf buffer) {
        return new SetDefaultEventProfilePacket(buffer.readUtf(64));
    }

    public static void handle(SetDefaultEventProfilePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !player.hasPermissions(2)) {
                return;
            }
            EventProfileSavedData data = EventProfileSavedData.get(player.serverLevel());
            if (data.profile(packet.profileId()).isPresent()) {
                data.setGlobalProfile(packet.profileId());
                player.displayClientMessage(Component.literal("Default KMD event profile set to: " + packet.profileId()), false);
            }
        });
        context.get().setPacketHandled(true);
    }
}
