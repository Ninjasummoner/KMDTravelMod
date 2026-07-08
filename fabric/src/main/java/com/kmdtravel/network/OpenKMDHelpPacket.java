package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public record OpenKMDHelpPacket() implements CustomPacketPayload {
    public static final OpenKMDHelpPacket INSTANCE = new OpenKMDHelpPacket();
    public static final Type<OpenKMDHelpPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "open_help"));
    public static final StreamCodec<FriendlyByteBuf, OpenKMDHelpPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
            },
            buffer -> INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenKMDHelpPacket packet, ClientPlayNetworking.Context context) {
        context.client().execute(ClientTravelScreens::openHelp);
    }
}
