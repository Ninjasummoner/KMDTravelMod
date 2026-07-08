package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TravelEventPromptPacket(String eventKey, String title, String description, boolean passive, int skipChancePercent, int ambushChancePercent, int waitSeconds) implements CustomPacketPayload {
    public static final Type<TravelEventPromptPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "travel_event_prompt"));
    public static final StreamCodec<FriendlyByteBuf, TravelEventPromptPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            TravelEventPromptPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(TravelEventPromptPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.eventKey());
        buffer.writeUtf(packet.title(), 128);
        buffer.writeUtf(packet.description(), 512);
        buffer.writeBoolean(packet.passive());
        buffer.writeVarInt(packet.skipChancePercent());
        buffer.writeVarInt(packet.ambushChancePercent());
        buffer.writeVarInt(packet.waitSeconds());
    }

    public static TravelEventPromptPacket decode(FriendlyByteBuf buffer) {
        return new TravelEventPromptPacket(buffer.readUtf(64), buffer.readUtf(128), buffer.readUtf(512), buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(TravelEventPromptPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTravelScreens.showEventPrompt(packet));
    }

    public Component titleComponent() {
        return Component.literal(title);
    }

    public Component descriptionComponent() {
        return Component.literal(description);
    }
}
