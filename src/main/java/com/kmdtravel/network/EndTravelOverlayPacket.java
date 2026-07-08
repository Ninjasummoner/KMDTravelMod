package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EndTravelOverlayPacket(boolean interrupted) implements CustomPacketPayload {
    public static final Type<EndTravelOverlayPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "end_travel_overlay"));
    public static final StreamCodec<FriendlyByteBuf, EndTravelOverlayPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            EndTravelOverlayPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(EndTravelOverlayPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.interrupted());
    }

    public static EndTravelOverlayPacket decode(FriendlyByteBuf buffer) {
        return new EndTravelOverlayPacket(buffer.readBoolean());
    }

    public static void handle(EndTravelOverlayPacket packet, IPayloadContext context) {
        context.enqueueWork(ClientTravelScreens::closeTravelProgress);
    }
}
