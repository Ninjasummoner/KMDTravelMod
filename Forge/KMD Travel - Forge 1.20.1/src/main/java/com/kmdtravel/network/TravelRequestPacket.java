package com.kmdtravel.network;

import com.kmdtravel.travel.FastTravelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record TravelRequestPacket(UUID sourceId, UUID destinationId) {
    public static void encode(TravelRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.sourceId());
        buffer.writeUUID(packet.destinationId());
    }

    public static TravelRequestPacket decode(FriendlyByteBuf buffer) {
        return new TravelRequestPacket(buffer.readUUID(), buffer.readUUID());
    }

    public static void handle(TravelRequestPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null) {
                FastTravelManager.requestTravel(player, packet.sourceId(), packet.destinationId());
            }
        });
        context.get().setPacketHandled(true);
    }
}

