package com.kmdtravel.network;

import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record TravelEventPromptPacket(String eventKey, String title, String description, boolean passive, int skipChancePercent, int ambushChancePercent, int waitSeconds) {

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

    public static void handle(TravelEventPromptPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ClientTravelScreens.showEventPrompt(packet));
        context.get().setPacketHandled(true);
    }

    public Component titleComponent() {
        return Component.literal(title);
    }

    public Component descriptionComponent() {
        return Component.literal(description);
    }
}
