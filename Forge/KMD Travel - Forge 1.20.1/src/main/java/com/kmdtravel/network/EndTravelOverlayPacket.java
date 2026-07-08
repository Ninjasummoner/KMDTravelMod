package com.kmdtravel.network;

import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EndTravelOverlayPacket(boolean interrupted) {
    public static void encode(EndTravelOverlayPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.interrupted());
    }

    public static EndTravelOverlayPacket decode(FriendlyByteBuf buffer) {
        return new EndTravelOverlayPacket(buffer.readBoolean());
    }

    public static void handle(EndTravelOverlayPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientTravelScreens::closeTravelProgress));
        context.get().setPacketHandled(true);
    }
}

