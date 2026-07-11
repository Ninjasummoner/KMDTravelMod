package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.travel.FastTravelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.UUID;

public record TravelRequestPacket(UUID sourceId, UUID destinationId) implements CustomPacketPayload {
    public static final Type<TravelRequestPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "travel_request"));
    public static final StreamCodec<FriendlyByteBuf, TravelRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            TravelRequestPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(TravelRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.sourceId());
        buffer.writeUUID(packet.destinationId());
    }

    public static TravelRequestPacket decode(FriendlyByteBuf buffer) {
        return new TravelRequestPacket(buffer.readUUID(), buffer.readUUID());
    }

    public static void handle(TravelRequestPacket packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
            FastTravelManager.requestTravel(player, packet.sourceId(), packet.destinationId());
        });
    }
}
