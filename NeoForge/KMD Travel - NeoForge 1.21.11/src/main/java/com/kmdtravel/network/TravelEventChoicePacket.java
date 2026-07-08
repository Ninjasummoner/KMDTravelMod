package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.travel.FastTravelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TravelEventChoicePacket(boolean viewEvent) implements CustomPacketPayload {
    public static final Type<TravelEventChoicePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "travel_event_choice"));
    public static final StreamCodec<FriendlyByteBuf, TravelEventChoicePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            TravelEventChoicePacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(TravelEventChoicePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.viewEvent());
    }

    public static TravelEventChoicePacket decode(FriendlyByteBuf buffer) {
        return new TravelEventChoicePacket(buffer.readBoolean());
    }

    public static void handle(TravelEventChoicePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                FastTravelManager.handleEventChoice(player, packet.viewEvent());
            }
        });
    }
}


