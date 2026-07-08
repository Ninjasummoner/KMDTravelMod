package com.kmdtravel.network;

import com.kmdtravel.travel.FastTravelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TravelEventChoicePacket(boolean viewEvent) {
    public static void encode(TravelEventChoicePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.viewEvent());
    }

    public static TravelEventChoicePacket decode(FriendlyByteBuf buffer) {
        return new TravelEventChoicePacket(buffer.readBoolean());
    }

    public static void handle(TravelEventChoicePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                FastTravelManager.handleEventChoice(player, packet.viewEvent());
            }
        });
        context.get().setPacketHandled(true);
    }
}

